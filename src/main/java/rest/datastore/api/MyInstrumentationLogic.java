
package rest.datastore.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CtClass;



/**
 * @author Igor Polevoy
 */
public class MyInstrumentationLogic {

    private String outputDirectory;

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void instrument(CtClass clazz) {
        if(outputDirectory == null){
            throw new RuntimeException("Property 'outputDirectory' must be provided");
        }

        try {
            System.out.println("**************************** START INSTRUMENTATION ****************************");
            System.out.println("Directory: " + outputDirectory);
            //InstrumentationModelFinder mf = null; //new InstrumentationModelFinder();
            File target = new File(outputDirectory);
            //mf.processDirectoryPath(target);
            ModelInstrumentation mi = new ModelInstrumentation();

            //for (CtClass clazz : mf.getModels()) {
                mi.instrument(clazz);
            //}
            generateModelsFile(clazz, target);
            System.out.println("**************************** END INSTRUMENTATION ****************************");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

   /* private static void generateModelsFile(List<CtClass> models, File target) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String modelsFileName = target.getAbsolutePath() + System.getProperty("file.separator") + "activejdbc_models.properties";
        FileOutputStream fout = new FileOutputStream(modelsFileName);

        for (CtClass model : models) {
            fout.write((model.getName() + ":" + getDatabaseName(model) + "\n").getBytes());
        }
        fout.close();
    }*/

    private static void generateModelsFile(CtClass model, File target) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String modelsFileName = target.getAbsolutePath() + System.getProperty("file.separator") + "activejdbc_models.properties";
        FileOutputStream fout = new FileOutputStream(modelsFileName);

        //for (CtClass model : models) {
            fout.write((model.getName() + ":" + getDatabaseName(model) + "\n").getBytes());
        //}
        fout.close();
    }
    
     static String getDatabaseName(CtClass model) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] annotations =  model.getAnnotations();

        for (Object annotation : annotations) {
            Class dbNameClass = Class.forName("org.javalite.activejdbc.annotations.DbName");
            if(dbNameClass.isAssignableFrom(annotation.getClass())){
                Method valueMethod = annotation.getClass().getMethod("value");
                return valueMethod.invoke(annotation).toString();
            }
        }
        return "default";
    }
}
