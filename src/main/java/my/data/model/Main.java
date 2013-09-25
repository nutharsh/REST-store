package my.data.model;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rest.datastore.api.ClassGenerator;


public class Main {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		//String client_name = "WGG";
		Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/authdb", "root", "opelin");
		
		String className = "auth_client_details";
		 ClassGenerator.generateClass(className);
		
		try {
			Class generateClass = Class.forName(className);
			Constructor[] declaredConstructors = generateClass.getDeclaredConstructors();
			for(Constructor c : declaredConstructors) {
				c.setAccessible(true);
			}
			generateClass.newInstance();
			org.javalite.activejdbc.annotations.Table annotation = (Table) generateClass.getAnnotation(org.javalite.activejdbc.annotations.Table.class);
			System.out.println(annotation.value());
			Method method = generateClass.getMethod("where", String.class, Object[].class);
			Object object = method.invoke( generateClass, " * ", new Object[] { } );
			System.out.println(object);
			if(object instanceof List) {
				List results = (List) object;
				Object object2 = results.get(0);
				System.out.println(object2.getClass());
				/*GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.setPrettyPrinting();
				gsonBuilder.serializeNulls();
				Gson gson = gsonBuilder.create();
				gson.toJson(object2);*/
				Method method2 = object2.getClass().getMethod("toJson", boolean.class, String[].class);
				Object json = method2.invoke(object2, true, new String[]{});
				System.out.println(json);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} 
	}
	
}

