package com.pikaqiu.scan;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

/**
 * @program: ct-foreign-parent
 * @description:
 * @author: xiaoye
 * @create: 2019-12-31 10:28
 **/
//@Component
public class RpcServerCore implements ApplicationContextAware {

  @Value(value = "${rpc.server}")
  private String rpcServerPath;
  @Autowired
  private ResourceLoader resourceLoader;
  @Autowired
  private DefaultListableBeanFactory defaultListableBeanFactory;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
    try {
      //获取路径下的类
      Resource[] resources = resolver.getResources("classpath*:" + rpcServerPath + "/*.class");
      for (Resource r : resources) {
        MetadataReader reader = metaReader.getMetadataReader(r);
        //判断是否代理类
//        boolean rpcServerCase = reader.getAnnotationMetadata().hasAnnotation(HttpRpcServer.class.getName());
//        if (rpcServerCase) {
          //获取代理类对象
//          Class<?> proxyClass = getClass().getClassLoader().loadClass(reader.getClassMetadata().getClassName());
          //获取代理后对象
//          Object interfaceInfo = ProxyFactory.getInterfaceInfo(proxyClass);
          //注册到spring
//          defaultListableBeanFactory.registerSingleton(StringUtils.lowerFirst(proxyClass.getSimpleName()), interfaceInfo);
//        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
