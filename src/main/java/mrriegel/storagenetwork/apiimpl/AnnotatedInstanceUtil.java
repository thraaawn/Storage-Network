package mrriegel.storagenetwork.apiimpl;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.IStorageNetworkPlugin;
import mrriegel.storagenetwork.api.StorageNetworkPlugin;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.util.*;

public class AnnotatedInstanceUtil {
    public static ASMDataTable asmDataTable;

    private AnnotatedInstanceUtil() {
    }

    public static List<IStorageNetworkPlugin> getPlugins() {
        return getInstances(StorageNetworkPlugin.class, IStorageNetworkPlugin.class);
    }

    private static <T> Map<Class<? extends T>, Map<String, Object>> getClassesWithAnnotations(Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        Map<Class<? extends T>, Map<String, Object>> classes = new HashMap<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                if(annotationInfo.containsKey("mod")) {
                    String requiredMod = (String)annotationInfo.get("mod");
                    if(requiredMod.length() > 0 && !Loader.isModLoaded(requiredMod)) {
                        continue;
                    }
                }

                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                classes.put(asmInstanceClass, annotationInfo);
            } catch (ClassNotFoundException | ExceptionInInitializerError e) {
                StorageNetwork.instance.logger.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }

        return classes;

    }

    private static <T> List<Class<? extends T>> getClasses(Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        List<Class<? extends T>> classes = new ArrayList<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                if(annotationInfo.containsKey("mod")) {
                    String requiredMod = (String)annotationInfo.get("mod");
                    if(requiredMod.length() > 0 && !Loader.isModLoaded(requiredMod)) {
                        continue;
                    }
                }

                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                classes.add(asmInstanceClass);
            } catch (ClassNotFoundException | ExceptionInInitializerError e) {
                StorageNetwork.instance.logger.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }

        return classes;
    }

    private static <T> Map<T, Map<String, Object>> getInstancesWithAnnotations(Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        Map<T, Map<String,Object>> instances = new HashMap<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                if(annotationInfo.containsKey("mod")) {
                    String requiredMod = (String)annotationInfo.get("mod");
                    if(requiredMod.length() > 0 && !Loader.isModLoaded(requiredMod)) {
                        continue;
                    }
                }

                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();

                instances.put(instance, annotationInfo);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ExceptionInInitializerError e) {
                StorageNetwork.instance.logger.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
        return instances;
    }

    private static <T> List<T> getInstances(Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        List<T> instances = new ArrayList<T>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                if(annotationInfo.containsKey("mod")) {
                    String requiredMod = (String)annotationInfo.get("mod");
                    if(requiredMod.length() > 0 && !Loader.isModLoaded(requiredMod)) {
                        continue;
                    }
                }

                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ExceptionInInitializerError e) {
                StorageNetwork.instance.logger.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
        return instances;
    }
}
