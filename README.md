# spring-boot-logger-starter

### IMPORTANT
##### If you interested to upload package to corporate artifactory, then read this block

You can install it to the corporate artifactory and use as package
I added needed configs to gradle file.
ARTIFACTORY_URL = url to your artifactory

Just add it as dependency to your build.gradle (or build.gradle.kts)
old gradle version


 <p>Add dependency
 <p> (for old gradle)</p> <pre>compile kz.progger.starter:spring-boot-starter-logger:0.0.3</pre> 
  (for new gradle) <pre>implementation kz.progger.starter:spring-boot-starter-logger:0.0.3</pre>from artifactory.progger.kz to your build.gradle file.
 

 <hr>
    <h4>Usage</h4>
    <br>
<b>@Logger</b> - logs path info, execution time, authorized username and passed arguments to the method

Add <b>@Logger</b> annotation on the top of your class, interface (including annotation type) declaration.

#####   1) If you don`t use <b>Spring Security</b> on your application, add to application.yml
    <pre>
    ...
    spring:
       autoconfigure:
             exclude: org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration</pre>
             <p>Without this config app will respond 401 "Unauthorized" error</p>
#####   2) Multithreads
   When multiple service hops are required to process a request or when multi-threading is implemented. <br>
   MDC data is passed to thread children, it doesnt work when threads are reused in a thread pool.
   That is why instead of ThreadPoolExecutor use <i>MdcThreadPoolExecutor</i> which will add context to the child threads. <br>

#####3) Rabbit and Akka
   When sending messages between servers, you have to manually set current requestId, because it will lost 
   
   <i>RabbitMQConfig Example: </i><br>
 
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
               RabbitTemplate rabbitTemplate = new RabbitTemplate();
               rabbitTemplate.setMessageConverter(jsonMessageConverter());
               rabbitTemplate.setConnectionFactory(connectionFactory);
               rabbitTemplate.setBeforePublishPostProcessors((MessagePostProcessor) message -> {
                   <b><i>message.getMessageProperties().setHeader("reqId", codaRequestId.get());</i></b>
                   return message;
               });
               return rabbitTemplate;
           }
    @Bean
        public SimpleRabbitListenerContainerFactory jsaFactory(ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
            SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
            configurer.configure(factory, connectionFactory);
            factory.setMessageConverter(jsonMessageConverter());
            factory.setConcurrentConsumers(30);
            factory.setMaxConcurrentConsumers(30);
            factory.setAdviceChain(org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                    .stateless()
                    .maxAttempts(10)
                    .backOffOptions(10_000, 2.0, 3600_000)
                    .build(),
                (MethodInterceptor) invocation -> {
                    Message message = (Message) invocation.getArguments()[1];
                    codaRequestId.set(message.getMessageProperties().getHeaders().get("reqId")
                        .toString());
                    return invocation.proceed();
                });
            return factory;
        }
 
(v2.0 - updates)
#### Version 0.0.2
<b>@Logger</b>

| argValue        | About           | Default Value  |
| ------------- |:-------------:| -----:|
| logArgValue     | Logging passed arguments to the method | true |

<b>@ExcludeLogger</b> - not log info on annotated method. It will help to avoid confidential information about customer or 
some configuration values. 
Useful [link](https://stackify.com/9-logging-sins-java/) about what we should not log.
<br>

#### Version 0.0.3
In version 0.0.3 you can regulate the size of arguments passed and responded from server

| argValue        | About           | Default Value  |
| ------------- |:-------------:| -----:|
| reqLogArgValLength     | Length of request arg to log | -1 |
| resLogArgValLength     | Length of response arg to log | -1 |

<br>

    
###Example 
<pre>
package kz.progger.example.HelloController;
...
import Logger;
...

@Controller
@RequestMapping("/customer")
@Logger
public class CustomerController {

    @GetMapping
    public String hello(@RequestParam String name) {
        return "Hello" + name;
    }
    
    @GetMapping("/log-arg-size")
    @Logger(reqLogArgValLength = 3, resLogArgValLength=3)
    public String hello(@RequestParam String name) {
        return "Hello" + name;
    }
    
    @GetMapping("/v2")
    @ExcludeLogger
    public String helloWithoutLog(@RequestParam String name) {
        return "Hello" + name;
    }
     
    @PostMapping("/signin")
    @Logger(logArgValue = false)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        ...
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt,tokenProvider.getAuthorities(authentication)));    
    }
}
</pre>

 
 And when you call this hello method you will see following info log details on your console:
 
 <pre>
2019-12-12 12:43:45,143 INFO  [kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.hello] requestId: 20191212_124345_008_W45 as anonymousUser with args: [{name}]
2019-12-12 12:43:45,609 INFO  [[kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.hello] requestId: 20191212_124345_008_W45 ok in [5 ms] as anonymousUser with result: [{Hello name}]

2019-12-12 12:43:45,143 INFO  [kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.hello] requestId: 20191212_124345_008_W45 as anonymousUser with args: [{na}]
2019-12-12 12:43:45,609 INFO  [[kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.hello] requestId: 20191212_124345_008_W45 ok in [5 ms] as anonymousUser with result: [{H}]

2019-12-12 12:43:48,143 INFO  [kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.authenticateUser] requestId: 20191212_124345_008_W45 as anonymousUser
2019-12-12 12:43:49,609 INFO  [[kz.progger.example.CustomerController] (http-nio-8080-exec-1) invoke [CustomerController.authenticateUser] requestId: 20191212_124345_008_W45 ok in [5 ms] as anonymousUser 

</pre>


  
  <h5><b>Solved Problems</b></h5>
  It will help you to control:
 <ul>
 <li>Log when method is called and ended</li> 
 <li>The incoming and outgoing arguments of this method</li> 
 <li>How much time it was longed</li> 
 </ul>
 
<p>To do this we used AOP. You can read detailed <a href= 
"https://docs.spring.io/spring/docs/2.5.x/reference/aop.html">here</a></p>
One of the benefit of the starter it has <b>RequestListener</b> which will generate <em>X-Request-Id</em> (unique Id for every request)
on every request. 
<p>Why its useful?</p>
 In our systems we need to deal with multiple clients simultaneously. On such system we can have different clients on multithreads. This requestId will help to developer increase their management overhead.
</p>

Now developer will not take care and waisting time writing logger on every method