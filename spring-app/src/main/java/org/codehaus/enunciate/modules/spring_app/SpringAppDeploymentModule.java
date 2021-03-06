/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.spring_app;

import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.spring_app.config.GlobalServiceInterceptor;
import org.codehaus.enunciate.modules.spring_app.config.HandlerInterceptor;
import org.codehaus.enunciate.modules.spring_app.config.SpringAppRuleSet;
import org.codehaus.enunciate.modules.spring_app.config.SpringImport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * <h1>Spring App Module</h1>
 *
 * <p>The spring app deployment module produces the configuration files and application extensions needed to apply
 * the <a href="http://www.springframework.org/">Spring</a> container to the Web service application.</p>
 *
 * <ul>
 * <li><a href="#steps">steps</a></li>
 * <li><a href="#config">application configuration</a></li>
 * <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The "generate" step generates the <a href="http://www.springframework.org/">Spring</a>
 * configuration file and application extensions.  Refer to <a href="#config">configuration</a>
 * to learn how to customize these things.</p>
 *
 * <p>The "generate" step is the only relevant step in the spring app deployment module.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <ul>
 * <li><a href="#config_structure">structure</a></li>
 * <li><a href="#config_attributes">attributes</a></li>
 * <li>elements<br/>
 * <ul>
 * <li><a href="#config_springImport">The "springImport" element</a></li>
 * <li><a href="#config_globalServiceInterceptor">The "globalServiceInterceptor" element</a></li>
 * <li><a href="#config_handlerInterceptor">The "handlerInterceptor" element</a></li>
 * <li><a href="#config_handlerMapping">The "handlerMapping" element</a></li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>The configuration for the Spring App deployment module is specified by the "spring-app" child element under the "modules" element
 * of the enunciate configuration file.</p>
 *
 * <h3><a name="config_structure">Structure</a></h3>
 *
 * <p>The following example shows the structure of the configuration elements for this module.  Note that this shows only the structure.
 * Some configuration elements don't make sense when used together.</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;spring-app contextLoaderListenerClass="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;applicationContextFilename="..." contextConfigLocation="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;springVersion="..."&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;springImport file="..." uri="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;springImport file="..." uri="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;globalServiceInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;globalServiceInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerInterceptor interceptorClass="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerMapping pattern="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;handlerMapping pattern="..." beanName="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/spring-app&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * </code>
 *
 * <h3><a name="config_attributes">attributes</a></h3>
 *
 * <ul>
 * <li>The "<b>contextLoaderListenerClass</b>" attribute specifies that FQN of the class to use as the Spring context loader listener.  The default is "org.springframework.web.context.ContextLoaderListener".</li>
 * <li>The "<b>applicationContextFilename</b>" attribute specifies the name of the Enunciate-generated application context file.  The default is "applicationContext.xml".</li>
 * <li>The "<b>contextConfigLocation</b>" attribute specifies the value of the contextConfigLocation init parameter supplied to the Spring
 *     <a href="http://static.springsource.org/spring/docs/2.0.x/api/org/springframework/web/context/ContextLoaderListener.html">ContextLoaderListener</a>.  The default is "/WEB-INF/" + <tt>applicationContextFilename</tt>.</li>
 * <li>The "springVersion" attribute specifies the spring version to use. If not set, an attempt will be made to autodetect it.</li>
 * </ul>
 *
 * <h3><a name="config_springImport">The "springImport" element</a></h3>
 *
 * <p>The "springImport" element is used to specify a spring configuration file that will be imported by the main
 * spring servlet config. It supports the following attributes:</p>
 *
 * <ul>
 * <li>The "file" attribute specifies the spring import file on the filesystem.  It will be copied to the WEB-INF directory.</li>
 * <li>The "uri" attribute specifies the URI to the spring import file.  The URI will not be resolved at compile-time, nor will anything be copied to the
 * WEB-INF directory. The value of this attribute will be used to reference the spring import file in the main config file.  This attribute is useful
 * to specify an import file on the classpath, e.g. "classpath:com/myco/spring/config.xml".</li>
 * </ul>
 *
 * <p>One use of specifying spring a import file is to wrap your endpoints with spring interceptors.  This can be done
 * by simply declaring a bean that is an instance of your endpoint class, which can be advised as needed.</p>
 *
 * <p>It's important to note that the type on which the bean context will be searched is the type of the endpoint <i>interface</i>, and then only if it exists.
 * If there are more than one beans that are assignable to the endpoint interface, the bean that is named the name of the service will be used.  Otherwise,
 * the deployment of your endpoint will fail.</p>
 *
 * <p>The same procedure can be used to specify the beans to use as REST endpoints.  In this case,
 * the bean context will be searched for each <i>REST interface</i> that the endpoint implements.  If there is a bean that implements that interface, it will
 * used instead of the default implementation.  If there is more than one, the bean that is named the same as the REST endpoint will be used.</p>
 *
 * <p>There also exists a mechanism to add certain AOP interceptors to all service endpoint beans.  Such interceptors are referred to as "global service
 * interceptors." This can be done by using the "globalServiceInterceptor" element (see below), or by simply creating an interceptor that implements
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice or org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor and declaring it in your
 * imported spring beans file.</p>
 *
 * <p>Each global interceptor has an order.  The default order is 0 (zero).  If a global service interceptor implements org.springframework.core.Ordered, the
 * order will be respected. As global service interceptors are added, it will be assigned a position in the chain according to it's order.  Interceptors
 * of the same order will be ordered together according to their position in the config file, with priority to those declared by the "globalServiceInterceptor"
 * element, then to instances of org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvice, then to instances of
 * org.codehaus.enunciate.modules.spring_app.EnunciateServiceAdvisor.</p>
 *
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/2.5.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h3><a name="config_globalServiceInterceptor">The "globalServiceInterceptor" element</a></h3>
 *
 * <p>The "globalServiceInterceptor" element is used to specify a Spring interceptor (instance of org.aopalliance.aop.Advice or
 * org.springframework.aop.Advisor) that is to be injected on all service endpoint beans.</p>
 *
 * <ul>
 * <li>The "interceptorClass" attribute specified the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 *
 * <h3><a name="config_handlerInterceptor">The "handlerInterceptor" element</a></h3>
 *
 * <p>The "handlerInterceptor" element is used to specify a Spring interceptor (instance of org.springframework.web.servlet.HandlerInterceptor)
 * that is to be injected on the handler mapping.</p>
 *
 * <ul>
 * <li>The "interceptorClass" attribute specifies the class of the interceptor.</p>
 * <li>The "beanName" attribute specifies the bean name of the interceptor.</p>
 * </ul>
 *
 * <p>For more information on spring bean configuration and interceptor advice, see
 * <a href="http://static.springframework.org/spring/docs/2.5.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h3><a name="config_handlerMapping">The "handlerMapping" element</a></h3>
 *
 * <p>The "handlerMapping" element is used to specify a custom Spring handler mapping.</p>
 *
 * <ul>
 * <li>The "pattern" attribute specifies the pattern that maps to the handler.</p>
 * <li>The "beanName" attribute specifies the bean name of the handler.</p>
 * </ul>
 *
 * <p>For more information on spring handler mappings, see
 * <a href="http://static.springframework.org/spring/docs/2.5.x/reference/index.html">the spring reference documentation</a>.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The spring app deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_spring_app.html
 */
public class SpringAppDeploymentModule extends FreemarkerDeploymentModule implements EnunciateClasspathListener {

  private final List<SpringImport> springImports = new ArrayList<SpringImport>();
  private final List<GlobalServiceInterceptor> globalServiceInterceptors = new ArrayList<GlobalServiceInterceptor>();
  private final List<HandlerInterceptor> handlerInterceptors = new ArrayList<HandlerInterceptor>();
  private String applicationContextFilename = "applicationContext.xml";
  private String contextConfigLocation = null;
  private String contextLoaderListenerClass = "org.springframework.web.context.ContextLoaderListener";
  private boolean enableSecurity = false;
  private boolean factoryBeanFound = false;
  private boolean spring3 = false;

  /**
   * @return "spring-app"
   */
  @Override
  public String getName() {
    return "spring-app";
  }

  /**
   * @return The URL to "spring-servlet.fmt"
   */
  protected URL getApplicationContextTemplateURL() {
    return SpringAppDeploymentModule.class.getResource("applicationContext.xml.fmt");
  }

  public void onClassesFound(Set<String> classes) {
    factoryBeanFound |= classes.contains("org.codehaus.enunciate.modules.spring_app.ServiceEndpointFactoryBean");
    //we'll key off the Converter class since that's new in Spring 3.
    spring3 |= classes.contains("org.springframework.core.convert.converter.Converter");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    if (!isDisabled() && isEnableSecurity()) {
      throw new EnunciateException("As of 1.23, enunciate-specific spring security configuration has been DEPRECATED and will be removed in a future release. Please see http://goo.gl/1S94J for more information.");
    }
  }

  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      if (!factoryBeanFound) {
        warn("The Spring module is enabled, but the Enunciate-Spring runtime classes weren't found on the Enunciate classpath. This could be fatal to the runtime application...");
      }
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    if (!enunciate.isUpToDateWithSources(getWebInfDir())) {
      EnunciateFreemarkerModel model = getModel();

      //standard spring configuration:
      model.put("springImports", getSpringImportURIs());
      model.put("applicationContextFilename", getApplicationContextFilename());
      model.put("spring3", this.spring3);
      Object docsDir = enunciate.getProperty("docs.webapp.dir");
      if (docsDir == null) {
        docsDir = "";
      }
      model.put("docsDir", docsDir);
      if (!globalServiceInterceptors.isEmpty()) {
        for (GlobalServiceInterceptor interceptor : this.globalServiceInterceptors) {
          if ((interceptor.getBeanName() == null) && (interceptor.getInterceptorClass() == null)) {
            throw new IllegalStateException("A global interceptor must have either a bean name or a class set.");
          }
        }
        model.put("globalServiceInterceptors", this.globalServiceInterceptors);
      }
      if (!handlerInterceptors.isEmpty()) {
        for (HandlerInterceptor interceptor : this.handlerInterceptors) {
          if ((interceptor.getBeanName() == null) && (interceptor.getInterceptorClass() == null)) {
            throw new IllegalStateException("A handler interceptor must have either a bean name or a class set.");
          }
        }
        model.put("handlerInterceptors", this.handlerInterceptors);
      }

      model.setFileOutputDirectory(getWebInfDir());
      processTemplate(getApplicationContextTemplateURL(), model);

      copySpringConfig();

    }
    else {
      info("Skipping generation of spring config files as everything appears up-to-date...");
    }

    BaseWebAppFragment webAppFragment = new BaseWebAppFragment(getName());
    webAppFragment.setBaseDir(getGenerateDir());

    ArrayList<String> servletListeners = new ArrayList<String>();
    servletListeners.add(getContextLoaderListenerClass());
    servletListeners.add("org.codehaus.enunciate.modules.spring_app.SpringComponentPostProcessor");
    webAppFragment.setListeners(servletListeners);

    Map<String, String> contextParams = new HashMap<String, String>();
    String contextConfigLocation = getContextConfigLocation();
    if (contextConfigLocation == null) {
      contextConfigLocation = "/WEB-INF/" + getApplicationContextFilename();
    }
    contextParams.put("contextConfigLocation", contextConfigLocation);
    webAppFragment.setContextParameters(contextParams);
    getEnunciate().addWebAppFragment(webAppFragment);
  }

  /**
   * Copy the spring application context and servlet config from the build dir to the WEB-INF directory.
   */
  protected void copySpringConfig() throws IOException {
    for (SpringImport springImport : springImports) {
      //copy the extra spring import files to the WEB-INF directory to be imported.
      if (springImport.getFile() != null) {
        File importFile = enunciate.resolvePath(springImport.getFile());
        String name = importFile.getName();
        name = resolveSpringImportFileName(name);
        enunciate.copyFile(importFile, new File(getWebInfDir(), name));
      }
    }
  }

  /**
   * Get the string form of the spring imports that have been configured.
   *
   * @return The string form of the spring imports that have been configured.
   */
  protected ArrayList<String> getSpringImportURIs() {
    ArrayList<String> springImportURIs = new ArrayList<String>(this.springImports.size());
    for (SpringImport springImport : springImports) {
      if (springImport.getFile() != null) {
        if (springImport.getUri() != null) {
          throw new IllegalStateException("A spring import configuration must specify a file or a URI, but not both.");
        }

        String fileName = new File(springImport.getFile()).getName();
        fileName = resolveSpringImportFileName(fileName);
        springImportURIs.add(fileName);
      }
      else if (springImport.getUri() != null) {
        springImportURIs.add(springImport.getUri());
      }
      else {
        throw new IllegalStateException("A spring import configuration must specify either a file or a URI.");
      }
    }
    return springImportURIs;
  }

  /**
   * Resolves the application context file name (in case there's a conflict).
   *
   * @param fileName The file name.
   * @return The resolved file name.
   */
  protected String resolveSpringImportFileName(String fileName) {
    if (!"applicationContext.xml".equals(getApplicationContextFilename())) {
      //if we're not using the default applicationContext.xml filename, we'll assume the user knows what they're doing.
      return fileName;
    }
    
    if ("applicationContext.xml".equalsIgnoreCase(fileName)) {
      fileName = "applicationContext-" + getModel().getEnunciateConfig().getLabel() + ".xml";
    }
    return fileName;
  }

  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getEnunciateConfig() != null && getModelInternal().getEnunciateConfig().getWebAppConfig() != null && getModelInternal().getEnunciateConfig().getWebAppConfig().isDisabled()) {
      debug("Module '%s' is disabled because the web application processing has been disabled.", getName());
      return true;
    }

    return false;
  }

  /**
   * The list of spring imports.
   *
   * @return The list of spring imports.
   */
  public List<SpringImport> getSpringImports() {
    return springImports;
  }

  /**
   * Add a spring import.
   *
   * @param springImports The spring import to add.
   */
  public void addSpringImport(SpringImport springImports) {
    this.springImports.add(springImports);
  }

  /**
   * Add a global service interceptor to the spring configuration.
   *
   * @param interceptorConfig The interceptor configuration.
   */
  public void addGlobalServiceInterceptor(GlobalServiceInterceptor interceptorConfig) {
    this.globalServiceInterceptors.add(interceptorConfig);
  }

  /**
   * Add a handler interceptor to the spring configuration.
   *
   * @param interceptorConfig The interceptor configuration.
   */
  public void addHandlerInterceptor(HandlerInterceptor interceptorConfig) {
    this.handlerInterceptors.add(interceptorConfig);
  }

  /**
   * The class to use as the context loader listener.
   *
   * @return The class to use as the context loader listener.
   */
  public String getContextLoaderListenerClass() {
    return contextLoaderListenerClass;
  }

  /**
   * The class to use as the context loader listener.
   *
   * @param contextLoaderListenerClass The class to use as the context loader listener.
   */
  public void setContextLoaderListenerClass(String contextLoaderListenerClass) {
    this.contextLoaderListenerClass = contextLoaderListenerClass;
  }

  /**
   * The name of the application context file.
   *
   * @return The name of the application context file.
   */
  public String getApplicationContextFilename() {
    return applicationContextFilename;
  }

  /**
   * The name of the application context file.
   *
   * @param applicationContextFilename The name of the application context file.
   */
  public void setApplicationContextFilename(String applicationContextFilename) {
    this.applicationContextFilename = applicationContextFilename;
  }

  /**
   * The context config location.
   *
   * @return The context config location.
   */
  public String getContextConfigLocation() {
    return contextConfigLocation;
  }

  /**
   * The context config location.
   *
   * @param contextConfigLocation The context config location.
   */
  public void setContextConfigLocation(String contextConfigLocation) {
    this.contextConfigLocation = contextConfigLocation;
  }

  /**
   * Whether to enable security.
   *
   * @return Whether to enable security.
   */
  public boolean isEnableSecurity() {
    return enableSecurity;
  }

  /**
   * Whether to enable security.
   *
   * @param enableSecurity Whether to enable security.
   */
  public void setEnableSecurity(boolean enableSecurity) {
    this.enableSecurity = enableSecurity;
  }

  /**
   * The spring version to use.
   *
   * @param version The spring version to use.
   */
  public void setSpringVersion(String version) {
    this.spring3 = version.startsWith("3");
  }

  /**
   * @return 200
   */
  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new SpringAppRuleSet();
  }

  @Override
  public Validator getValidator() {
    return null;
  }

  /**
   * The directory where the config files are generated.
   *
   * @return The directory where the config files are generated.
   */
  protected File getWebInfDir() {
    return new File(getGenerateDir(), "WEB-INF");
  }

}
