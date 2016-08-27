package com.xtonic.xml2javaBeanUtil;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;

import net.sf.json.xml.XMLSerializer;


public class XML2JaveBean {

	private static final ConcurrentHashMap<String, Map<String, Field>> BEAN_FIELDS_CACHE = new ConcurrentHashMap<String, Map<String, Field>>();

	private static final String DATETIME_REG = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{7}((\\+|-)\\d{2}:\\d{2})?";

	private static final String DATE_REG = "\\d{4}[-/]\\d{2}[-/]\\d{2}";

	private static final String DEFAULT_STRING_VALUE = "";

	private XML2JaveBean() {
	}

	public static <T> T XmlToJavaBean(InputStream xmlInputStream,Class<T> clazz) throws Exception {
		String xmlStr = new XMLSerializer().readFromStream(xmlInputStream).toString();
		JSONObject xmlJson = JSONObject.parseObject(xmlStr);
		 System.out.println(xmlJson);
		 return (T) pareseJSon(xmlJson, clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> T pareseJSon(Map<String, Object> context, Class<T> clazz) throws Exception {
		T obj = clazz.newInstance();
		boolean isConllection = false;
		boolean isMap = false;
		Map<String, Field> fieldsCache = getFields(clazz);
		Field field = null;
		CollectionType collectionClass = null;
		Object tmpOjbect = null;
		String fieldKeyStr = null;
		for (String key : context.keySet()) {
			if (key.startsWith("@")) {
				fieldKeyStr = key.substring(1);
			} else {
				fieldKeyStr = key;
			}
			field = fieldsCache.get(fieldKeyStr.toUpperCase());
			// 判断XML里面的节点是否在实体类里面有对应的属性；
			if (field == null) {
				continue;
			}
			;

			// 获取属性对应的 set方法
			String methodName = getMethodName(field);
			Method setValueMethod = clazz.getDeclaredMethod(methodName, field.getType());

			// 如果XML数据节点对应的是为一个空值 ，则给他设置一个默认值；
			// 目前只针对STRING类型的属性设置默认值；
			if (context.get(key) == null) {
				setDefaultValue(obj, field, setValueMethod);
				continue;
			}

			// 判断该节点是常规的，还是一个集合类型的；
			isMap = Map.class.isAssignableFrom(context.get(key).getClass());
			isConllection = Collection.class.isAssignableFrom(context.get(key).getClass());
			List<Map<String, Object>> list = null;
			if (isConllection || isMap) {
				List<Object> subObjList = new ArrayList<Object>();
				// 获取集合属性上面对应的注解
				collectionClass = field.getAnnotation(CollectionType.class);
				if (collectionClass == null) {
					throw new RuntimeException(clazz.getName() + "实体类的集合属性" + field.getName() + "没有添加注解 。");
				}
				if(context.get(key) instanceof List){
					List<Map<String,Object>> tmpList = (List<Map<String, Object>>) context.get(key);
					for (Map<String, Object> subContext : tmpList ) {
						subObjList.add(pareseJSon((Map) subContext, Class.forName(collectionClass.className())));
					}
					setValueMethod.invoke(obj, subObjList);
					continue;
				}else{
					tmpOjbect = ((Map) context.get(key)).get(collectionClass.elementNode());
				}
				
				if (tmpOjbect instanceof List) {
					list = (List) tmpOjbect;
					for (Map<String, Object> subContext : list) {
						subObjList.add(pareseJSon((Map) subContext, Class.forName(collectionClass.className())));
					}
					setValueMethod.invoke(obj, subObjList);
					continue;
				}
				Map<String, Object> subContext = (Map) tmpOjbect;
				subObjList.add(pareseJSon(subContext, Class.forName(collectionClass.className())));
				setValueMethod.invoke(obj, subObjList);
				continue;
			}
			// set非集合类型的属性的值
			setValueMethod.invoke(obj, paramTypeTransfer(context.get(key), field.getType()));
			System.out.println(key + " : " + context.get(key));
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private static <T> T paramTypeTransfer(Object obj, Class<T> clazz) throws ParseException {
		if (clazz == String.class) {
			return (T) obj;
		}

		if (clazz == int.class || clazz == Integer.class) {
			return (T) Integer.valueOf((String) obj);
		}

		if (clazz == double.class || clazz == Double.class) {
			return (T) Double.valueOf((String) obj);
		}

		if (clazz == float.class || clazz == Float.class) {
			return (T) Float.valueOf((String) obj);
		}

		if (clazz == long.class || clazz == Long.class) {
			return (T) Long.valueOf((String) obj);
		}

		if (clazz == boolean.class || clazz == Boolean.class) {
			if ("false".equals((String) obj)) {
				return (T) Boolean.FALSE;
			}
			if ("true".equals((String) obj)) {
				return (T) Boolean.TRUE;
			}
			throw new RuntimeException("Boolean类型的值错误，只能是 'false' or 'true'");
		}

		if (clazz == Date.class) {
			String tmpStr = (String) obj;
			if (tmpStr.matches(DATETIME_REG)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				return (T) sdf.parse(tmpStr.substring(0, tmpStr.indexOf('.')).replace('T', ' '));
			}
			if (tmpStr.matches(DATE_REG)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				return (T) sdf.parse(tmpStr);
			}
			throw new RuntimeException(tmpStr + " 时间的格式不对");
		}

		return null;
	}

	private static String getMethodName(Field propertyField) {
		// javabean规范中: boolean类型属性的 setter方法名的处理
		String property = propertyField.getName();
		if (propertyField.getType() == boolean.class && property.toUpperCase().startsWith("IS")) {
			String tmpStr = property.substring(2);
			return "set" + tmpStr.substring(0, 1).toUpperCase() + tmpStr.substring(1);
		}
		// javabean规范中 对于属性名中第一字母为大写，setter方法名的处理
		if (Character.isUpperCase(property.charAt(0))) {
			return "set" + property;
		}
		// javabean规范中 对于属性名中第二字母为大写，setter方法名的处理
		if (Character.isUpperCase(property.charAt(1))) {
			return "set" + property;
		}

		return "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
	}

	private static Map<String, Field> getFields(Class clazz) {
		Map<String, Field> fieldsCache = BEAN_FIELDS_CACHE.get(clazz.getName());
		if (fieldsCache == null) {
			Map<String, Field> fieldsMap = new HashMap<String, Field>();
			Field[] fields = clazz.getDeclaredFields();
			for (Field tmpField : fields) {
				fieldsMap.put(tmpField.getName().toUpperCase(), tmpField);
			}
			BEAN_FIELDS_CACHE.putIfAbsent(clazz.getName(), fieldsMap);
			return fieldsMap;
		}
		return fieldsCache;
	}

	private static void setDefaultValue(Object obj, Field field, Method setValueMethod) throws Exception {
		Class paramterClazz = field.getType();
		if (paramterClazz == String.class) {
			setValueMethod.invoke(obj, DEFAULT_STRING_VALUE);
		}

		/*
		 * if(Number.class.isAssignableFrom(paramterClazz)){
		 * setValueMethod.invoke(obj, 0); }
		 * 
		 * if(paramterClazz == int.class || paramterClazz == double.class ||
		 * paramterClazz == float.class || paramterClazz == long.class){
		 * setValueMethod.invoke(obj, 0); }
		 */
	}

}
