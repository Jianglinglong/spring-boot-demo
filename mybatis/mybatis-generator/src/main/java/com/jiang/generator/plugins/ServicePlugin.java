package com.jiang.generator.plugins;

import com.jiang.generator.enums.Config;
import com.jiang.generator.enums.ImportType;
import com.jiang.generator.utils.ClassUtil;
import com.jiang.generator.utils.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jiang
 * @date 2018/12/13
 * @time 15:27
 **/
public class ServicePlugin extends PluginAdapter {
    private String servicePackage;
    private String serviceProject;
    private String serviceRoot;
    private String defaultServiceImpl;
    private String serviceName;
    private String superServiceName;
    private final Logger logger = LoggerFactory.getLogger(ServicePlugin.class);
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        servicePackage = properties.getProperty(Config.SERVICE_PACKAGE.getProperty(), "com.jiang.web.service");
        serviceProject = properties.getProperty(Config.SERVICE_PROJECT.getProperty(), "src/main/java");
        serviceRoot = properties.getProperty(Config.CLIENT_ROOT_INTERFACE.getProperty(), "com.jiang.web.service.BaseService");
        String[] split = serviceRoot.split("\\.");
        superServiceName = split[split.length-1];
        defaultServiceImpl = properties.getProperty(Config.SERVICE_IMPL.getProperty(), "com.jiang.web.service.DefaultService");
        String[] name = this.defaultServiceImpl.split("\\.");
        serviceName = name[name.length-1];
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }


    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> services = new ArrayList<>();
        JavaFormatter javaFormatter = ObjectFactory.createJavaFormatter(context);
        try {
            generateServiceInterFace(introspectedTable, services, javaFormatter);
        }catch (ArrayIndexOutOfBoundsException e){
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
           logger.error(tableName+" does not have Primary Key,Service not generated");
        }
        return services;
    }

    private void generateServiceInterFace(IntrospectedTable introspectedTable, List<GeneratedJavaFile> files, JavaFormatter javaFormatter) {
        IntrospectedColumn column = introspectedTable.getPrimaryKeyColumns().get(0);
        FullyQualifiedJavaType keyFullType = column.getFullyQualifiedJavaType();
        String keyFullTypeShortName = keyFullType.getShortName();
        String model = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String modelPackage = this.context.getJavaModelGeneratorConfiguration().getTargetPackage();
        String service = this.servicePackage + "." + model + "Service";
        String serviceImpl = this.servicePackage + ".impl." + model + "ServiceImpl";
        String baseService = String.format("%s<%s,%sExample>", superServiceName, model,  model);
        String defaultServiceImpl = String.format("%s<%s,%sExample>", serviceName, model, model);
        Class serviceClass = ClassUtil.getClassForName(service);
        Class serviceClassImpl = ClassUtil.getClassForName(serviceImpl);
        //service 、service imp 公共导包set
        Set<FullyQualifiedJavaType> types = new TreeSet<>();
        if (serviceClass == null || serviceClassImpl == null) {
            types.add(keyFullType);
            types.add(new FullyQualifiedJavaType(modelPackage + "." + model));
            types.add(new FullyQualifiedJavaType(modelPackage + "." + model + "Example"));
        }
        if (serviceClass == null) {
            //service接口
            Interface serviceInterface = new Interface(service);
            serviceInterface.setVisibility(JavaVisibility.PUBLIC);
            serviceInterface.addImportedTypes(types);
            serviceInterface.addImportedType(new FullyQualifiedJavaType(serviceRoot));
            serviceInterface.addSuperInterface(new FullyQualifiedJavaType(baseService));

            //添加service到额外生成的文件集合
            files.add(new GeneratedJavaFile(serviceInterface, this.serviceProject, javaFormatter));
        }
        if (serviceClassImpl == null) {
            //service实现类
            TopLevelClass topLevelClass = new TopLevelClass(serviceImpl);
            topLevelClass.setVisibility(JavaVisibility.PUBLIC);
            topLevelClass.addImportedTypes(types);
            //引入service
            topLevelClass.addImportedType(new FullyQualifiedJavaType(service));
            topLevelClass.addImportedType(new FullyQualifiedJavaType(this.defaultServiceImpl));
            //添加父类
            topLevelClass.setSuperClass(defaultServiceImpl);
            topLevelClass.addSuperInterface(new FullyQualifiedJavaType(model + "Service"));
            topLevelClass.addAnnotation(ImportType.SPRING_SERVICE.getAnnotation());
            topLevelClass.addImportedType(new FullyQualifiedJavaType(ImportType.SPRING_SERVICE.getType()));
            files.add(new GeneratedJavaFile(topLevelClass, this.serviceProject, javaFormatter));
        }
    }


}
