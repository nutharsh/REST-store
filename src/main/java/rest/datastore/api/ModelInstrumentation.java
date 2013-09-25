package rest.datastore.api;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.javalite.instrumentation.InstrumentationException;


public class ModelInstrumentation{

    private CtClass modelClass;


    public ModelInstrumentation() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        modelClass = ClassPool.getDefault().get("org.javalite.activejdbc.Model");
    }

    public void instrument(CtClass modelClass) throws InstrumentationException {

        try {
            addDelegates(modelClass);
            CtMethod m = CtNewMethod.make("public static String getClassName() { return \"" + modelClass.getName() + "\"; }", modelClass);
            CtMethod getClassNameMethod = modelClass.getDeclaredMethod("getClassName");
            modelClass.removeMethod(getClassNameMethod);
            modelClass.addMethod(m);
            //String out = getOutputDirectory(modelClass);
            //addSerializationSupport(modelClass);
            System.out.println("Instrumented class: " + modelClass.getName() );
            modelClass.writeFile(new File("./target/classes/").getCanonicalPath());
        }
        catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    /*private String getOutputDirectory(CtClass modelClass) throws NotFoundException, URISyntaxException {
        URL u = modelClass.getURL();
        String file = u.getFile();
        file = file.substring(0, file.length() - 6);
        String className = modelClass.getName();
        className = className.replace(".", "/");
        return file.substring(0, file.indexOf(className));
    }*/

    private void addDelegates(CtClass target) throws NotFoundException, CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        CtMethod[] targetMethods = target.getDeclaredMethods();
        for (CtMethod method : modelMethods) {

            if(Modifier.PRIVATE == method.getModifiers()){
                continue;
            }
            CtMethod newMethod = CtNewMethod.delegator(method, target);

			// Include the generic signature
			for (Object attr : method.getMethodInfo().getAttributes()) {
				if (attr instanceof javassist.bytecode.SignatureAttribute) {
					javassist.bytecode.SignatureAttribute signatureAttribute = (javassist.bytecode.SignatureAttribute) attr;
					newMethod.getMethodInfo().addAttribute(signatureAttribute);
				}
			}

            if (!targetHasMethod(targetMethods, newMethod)) {
                target.addMethod(newMethod);
            }
            else{
                System.out.println("Detected method: " + newMethod.getName() + ", skipping delegate.");
            }
        }

    }

    //TODO: remove unused methods later
    private void addSerializationSupport(CtClass target) throws CannotCompileException, NotFoundException {

        CtMethod m = CtNewMethod.make(
                "private void writeObject(java.io.ObjectOutputStream out)  {\n" +
                "        out.writeObject(toMap());\n" +
                "}", target);

        CtClass ioException = ClassPool.getDefault().get("java.io.IOException");
        CtClass classNotFoundException = ClassPool.getDefault().get("java.lang.ClassNotFoundException");

        m.setExceptionTypes(new CtClass[]{ioException});
        target.addMethod(m);

        m = CtNewMethod.make(
                "private void readObject(java.io.ObjectInputStream in) {\n" +
                        "        fromMap((java.util.Map)in.readObject());\n" +
                        "    }", target);
        m.setExceptionTypes(new CtClass[]{ioException, classNotFoundException});
        target.addMethod(m);
    }

    private CtMethod createFindById(CtClass clazz) throws CannotCompileException {
        String body = "public static "+ clazz.getName() +" findById(Object obj)\n" +
                "        {\n" +
                "            return (" + clazz.getName() + ")org.javalite.activejdbc.Model.findById(obj);\n" +
                "        }";
        return CtNewMethod.make(body, clazz);
    }

    private CtMethod createFindFirst(CtClass clazz) throws CannotCompileException {
        String body = " public static " + clazz.getName() + " findFirst(String s, Object params[])\n" +
                "   {\n" +
                "       return (" + clazz.getName() + ")org.javalite.activejdbc.Model.findFirst(s, params);\n" +
                "   }";
        return CtNewMethod.make(body, clazz);
    }

    private boolean targetHasMethod(CtMethod[] targetMethods, CtMethod delegate) {
        for (CtMethod targetMethod : targetMethods) {
            if (targetMethod.equals(delegate)) {
                return true;
            }
        }
        return false;
    }
}
