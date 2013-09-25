package rest.datastore.api;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

public class ClassGenerator {
	
	@SuppressWarnings("rawtypes")
	public static Class generateClass(String className) {
		ClassPool classPool = ClassPool.getDefault();
		CtClass makeClass = classPool.makeClass(className);
		
		Class newlyCreatedClass = null;
		try {
			makeClass.setSuperclass(classPool.get(org.javalite.activejdbc.Model.class.getName()));
			//makeClass.addConstructor(new CtConstructor(null, makeClass));
			CtConstructor[] constructors = makeClass.getConstructors();
			/*for(CtConstructor ctConstructor : constructors) {
				ctConstructor.setModifiers(Modifier.PUBLIC);
			}*/
			//CtClass serviceCtClass = classPool.getCtClass(Object.class.getName());
            CtConstructor ctConstructor = new CtConstructor(new CtClass[]{}, makeClass);
            ctConstructor.setModifiers(Modifier.PUBLIC);
            ctConstructor.setBody("super();");
            makeClass.addConstructor(ctConstructor);
            
            CtConstructor[] constructors2 = makeClass.getConstructors();
            
			ClassFile classFile = makeClass.getClassFile();
			
			ConstPool constpool = classFile.getConstPool();
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			Annotation annot = new Annotation("org.javalite.activejdbc.annotations.Table", constpool);
			annot.addMemberValue("value", new StringMemberValue(className, classFile.getConstPool()));
			attr.setAnnotation(annot);
			classFile.addAttribute(attr);
			//classFile.setVersionToJava5();
			 // add the annotation to the method descriptor
			 //sayHelloMethodDescriptor.getMethodInfo().addAttribute(attr);
			System.out.println(makeClass.getName());
			
			makeClass.defrost();
			/*System.out.println(newlyCreatedClass.getSuperclass());
			Method[] methods = newlyCreatedClass.getMethods();
			for(Method m : methods) {
				System.out.println(m.getName());
			}*/
			MyInstrumentationLogic instrumentationLogic = new MyInstrumentationLogic();
			instrumentationLogic.setOutputDirectory("./target/classes/");
			instrumentationLogic.instrument(makeClass);
			newlyCreatedClass = makeClass.toClass();
			System.out.println(newlyCreatedClass.getName());
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		
		return newlyCreatedClass;
	}
	
}
