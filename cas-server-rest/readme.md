[博客原文:CAS之5.2x版本之REST验证ticket_跨系统访问资源](https://blog.csdn.net/yelllowcong/article/details/79290916)




> Cas中REST的方式来验证Ticket，这种场景是解决A系统需要访问B系统需要登录权限的资源的，并不是用来解决 登录问题，这点必须很清楚。我刚开始就是不明白这个，才被坑的。在这个里面会涉及到TGT(Ticket Granting Token ，票据生成口令)，哈有ST(Service ticket，服务ticket),这两个东西，TGT是有一定的存活期，而ST只有一次机会，访问后就会失效。获取ticket的服务目标地址同，访问的地址必须一致，不然就会报st无效的问题

REST原理
------

首先客户端提交用户名、密码、及Service三个参数， 如果验证成功便返回用户的TGT(Ticket Granting Ticket)至客户端, 然后客户端再根据 TGT 获取用户的 ST(Service Ticket)来进行验证登录。 故名思意，TGT是用于生成一个新的Ticket(ST)的Ticket，而ST则是提供给客户端用于登录的Ticket，两者最大的区别在于， TGT是用户名密码验证成功之后所生成的Ticket，并且会保存在Server中及Cookie中，而ST则必须是是根据TGT来生成，主要用于登录，并且当登录成功之后 ST 则会失效。通过访问服务地址[http://xxx?ticket=xx](http://xxx?ticket=xx)

项目代码
----

    https://gitee.com/yellowcong/springboot_cas/tree/master/cas-server-rest

### 系统架构

节点

作用

[https://cas.example.org:9000](https://cas.example.org:9000)

cas服务器

[http://cas.example.org.com:9001](http://cas.example.org.com:9001)

客户端

服务端配置
-----

### 添加rest请求的依赖

如果需要使用rest的请求方式，就需要添加下面的依赖。

    <!--开启cas server的rest支持-->
     <dependency>
         <groupId>org.apereo.cas</groupId>
         <artifactId>cas-server-support-rest</artifactId>
         <version>${cas.version}</version>
     </dependency>

客户端
---

### cas工具包

用于直接过去到ST(Service Ticket)，然后返回给用户，在登录到服务器上。可以通过st来实现免密码登录，这个可以在用户注册的场景中使用。

    package com.yellowcong.cas;
    
    
    import javax.net.ssl.HttpsURLConnection;
    
    import org.springframework.util.StringUtils;
    
    import java.io.*;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.net.URLConnection;
    import java.net.URLEncoder;
    
    /**
     * 首先客户端提交用户名、密码、及Service三个参数，
     * 如果验证成功便返回用户的TGT(Ticket Granting Ticket)至客户端, 
     * 然后客户端再根据 TGT 获取用户的 ST(Service Ticket)来进行验证登录。 
     * 故名思意，TGT是用于生成一个新的Ticket(ST)的Ticket，
     * 而ST则是提供给客户端用于登录的Ticket，两者最大的区别在于，
     * TGT是用户名密码验证成功之后所生成的Ticket，并且会保存在Server中及Cookie中，
     * 而ST则必须是是根据TGT来生成，主要用于登录，并且当登录成功之后 ST 则会失效。
     * 创建日期:2018年2月8日
     * 创建时间:下午6:39:29
     * 创建者    :yellowcong
     * 机能概要:
     */
    public class CasServerUtil {
    
        //登录服务器地址
        private static final String  CAS_SERVER_PATH = "https://cas.example.org:9000";
    
        //登录地址的token 
        private static final String  GET_TOKEN_URL = CAS_SERVER_PATH + "/v1/tickets";
    
        //目标返回的服务器的url， 同访问的地址必须完全一致，不然就会报错。
        private static final String TAGET_URL = "http://yellowcong.com:8888/";
    
        private static CasServerUtil utils = null;
    
        private CasServerUtil(){}
    
        public static CasServerUtil getInstance(){
            if(utils == null){
                synchronized (CasServerUtil.class) {
                    if(utils == null){
                        utils = new CasServerUtil();
                    }
                }
            }
            return utils;
        }
        /**
          * 创建日期:2018/02/08<br/>
          * 创建时间:15:35:16<br/>
          * 创建用户:yellowcong<br/>
          * 机能概要: 先通过用户名密码，
          * 先生成tikect的 token，然后再通过token获取到id
          * @param args
          * @throws Exception
         */
        public static void main(String [] args) throws Exception {
    
    
            String username ="yellowcong";
            String password ="yellowcong";
    
    
            CasServerUtil utils = CasServerUtil.getInstance();
    
            String st = utils.getSt(username, password);
            System.out.println(st);
        }
        /**
         * 创建日期:2018年2月8日<br/>
         * 创建时间:下午7:26:32<br/>
         * 创建用户:yellowcong<br/>
         * 机能概要:通过用户名和密码来获取service ticket,通过这个可以免密码登录
         * @param username
         * @param password
         * @return
         */
        public String getSt(String username,String password){
            //先获取到 token generate ticket
            String tgt = utils.getTGT(username, password);
    
            if(StringUtils.isEmpty(tgt)){
                return "";
            }
    
            return utils.getST(tgt);
        }
        /**
         * 创建日期:2018年2月8日<br/>
         * 创建时间:下午6:36:54<br/>
         * 创建用户:yellowcong<br/>
         * 机能概要:获取到 （Tokent generate tiker ,token生成票据）tgt
         * @return
         */
        private String getTGT(String username,String password){
            String tgt = "";
            OutputStreamWriter out = null;
            BufferedWriter wirter  = null;
            HttpsURLConnection conn = null;
            try {
                //第一步，获取到tgt
                conn = (HttpsURLConnection) openConn(GET_TOKEN_URL);
                String param ="username=" + URLEncoder.encode(username, "UTF-8");
                param += "&password" + "=" + URLEncoder.encode(password, "UTF-8");
    
                out = new OutputStreamWriter(conn.getOutputStream());
                wirter = new BufferedWriter(out);
                //添加参数到目标服务器
                wirter.write(param);
                wirter.flush();
                wirter.close();
                out.close(); 
    
                //获取token
                tgt = conn.getHeaderField("location");
                //获取返回值
                if (tgt != null && conn.getResponseCode() == 201) {
                      tgt = tgt.substring(tgt.lastIndexOf("/") + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return tgt;
        }
    
        /**
         * 创建日期:2018年2月8日<br/>
         * 创建时间:下午7:15:16<br/>
         * 创建用户:yellowcong<br/>
         * 机能概要:根据票据生成工具，获取st
         * @param tgt
         * @return
         */
        private String getST(String tgt){
            String serviceTicket = "";
            OutputStreamWriter out = null;
            BufferedWriter wirter  = null;
            HttpsURLConnection conn = null;
            try {
    
                //第一步，获取到tgt
                conn = (HttpsURLConnection) openConn(GET_TOKEN_URL+"/"+tgt);
    
                //需要访问的目标网站
                String param ="service=" + URLEncoder.encode(TAGET_URL, "utf-8");
    
                out = new OutputStreamWriter(conn.getOutputStream());
                wirter = new BufferedWriter(out);
                //添加参数到目标服务器
                wirter.write(param);
                wirter.flush();
                wirter.close();
                out.close(); 
    
                //获取返回的ticket票据
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line ="";
                  while ((line = in.readLine()) != null) {
                      serviceTicket = line;
                  }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return serviceTicket;
        }
    
        /**
         * 创建日期:2018年2月8日<br/>
         * 创建时间:下午7:28:36<br/>
         * 创建用户:yellowcong<br/>
         * 机能概要:通过post表单提交来获取连接
         * @param urlk
         * @return
         * @throws Exception
         */
        private URLConnection openConn(String urlk) throws Exception {
            URL url = new URL(urlk);
            HttpsURLConnection hsu = (HttpsURLConnection) url.openConnection();
            hsu.setDoInput(true);
            hsu.setDoOutput(true);
            hsu.setRequestMethod("POST");
            return hsu;
        }
    }
    

从下图（CAS服务器的后台），可以看出，是先创建创建了ticket,然后再去验证的。  
![这里写图片描述](https://img-blog.csdn.net/20180208204638860?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWVsbGxvd2Nvbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 控制器

我这个地方，未登录的用户，通过访问/user/login/input这个控制器，来直接访问需要的登录的资源，通过后面家上ticket的方式来传递参数。

    @RequestMapping("/login/input")
    public String login(HttpServletRequest req,HttpServletResponse resp) throws Exception {
    
        //获取票据信息
        String ticket = CasServerUtil.getInstance().getSt("yellowcong", "yellowcong");
        return "redirect:http://cas.example.org.com:9001?ticket="+ticket;
    }

测试
--

    http://cas.example.org.com:9001/user/login/input

我开始没有登录的时候，直接就跳转到了登录页面，当我访问那个设有动态获取ST的，直接就到了系统页面，但是点击登录1，也就是本系统，结果又跑去登录，意味着，ST失效了。

![这里写图片描述](https://img-blog.csdn.net/20180208213853080?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWVsbGxvd2Nvbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

一个典型的调用流程
---------

以下是某个应用系统使用cas_service包接口的典型流程，通过rest访问流程，：

     1、某用户登录应用A，因为是首次登录，需提供用户名、密码；
     2、应用A根据用户名、密码，调用getTicketGrantingTicket接口获取TGT；
     3、TGT多次使用，需保存在session或其它存储对象中；
     4、应用A使用TGT，调用getServiceTicket接口获取am服务的ST；
     5、应用A可使用刚获取的ST，作为参数访问am服务；
     6、ST因有效期短暂且使用次数有限制，一般是一次性使用，不必保存；
     7、用户欲访问应用B的bn服务，先从session或其它存储对象中查找到TGT；
     8、应用A（或应用B）TGT，调用getServiceTicket接口获取bn服务的ST；
     9、应用B接收ST，调用verifySeviceTicket接口，返回不为null则该ST有效；
     10、验证通过后，应用B使用该ST访问bn服务；
     11、应用B可调用接口getCasUserName和getCasAttributes，获取登录用户及相关属性；
     12、欲根据ST查找当前登录用户，调用getUsernameSeviceTicket接口，返回值即是；
     13、用户从某应用注销时，需调用deleteTicketGrantingTicket接口从Cas Server删除TGT。
    

常见错误
----

### org.jasig.cas.client.validation.TicketValidationException: 未能够识别出目标 ‘ST-4-O14fKb2EIFARsDXUzWBqQaLRyTE-yellowcong-pc’票根

没有识别票据，说明票据已经失效了

    org.jasig.cas.client.validation.TicketValidationException: 未能够识别出目标 'ST-4-O14fKb2EIFARsDXUzWBqQaLRyTE-yellowcong-pc'票根
        at org.jasig.cas.client.validation.Cas20ServiceTicketValidator.parseResponseFromServer(Cas20ServiceTicketValidator.java:84) ~[cas-client-core-3.5.0.jar:3.5.0]
        at org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator.validate(AbstractUrlBasedTicketValidator.java:201) ~[cas-client-core-3.5.0.jar:3.5.0]
        at org.jasig.cas.client.validation.AbstractTicketValidationFilter.doFilter(AbstractTicketValidationFilter.java:204) ~[cas-client-core-3.5.0.jar:3.5.0]
    

![这里写图片描述](https://img-blog.csdn.net/20180208193249332?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWVsbGxvd2Nvbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

直接加上票据，结果就是不识别啊  
![这里写图片描述](https://img-blog.csdn.net/20180208193412848?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWVsbGxvd2Nvbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### org.jasig.cas.client.validation.TicketValidationException: 票根’ST-6-wv9-gX065lK8K7FFryQi1K0JSRo-yellowcong-pc’不符合目标服务

不符合目标服务，我去你大爷。导致这个问题的原因是，过滤器中，配置的url地址和当前返回的票据的url地址对不上，所导致的。这个url，需要加上 反斜杠啊，据花藤。

    org.jasig.cas.client.validation.TicketValidationException: 票根'ST-6-wv9-gX065lK8K7FFryQi1K0JSRo-yellowcong-pc'不符合目标服务
        at org.jasig.cas.client.validation.Cas20ServiceTicketValidator.parseResponseFromServer(Cas20ServiceTicketValidator.java:84) ~[cas-client-core-3.5.0.jar:3.5.0]
        at org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator.validate(AbstractUrlBasedTicketValidator.java:201) ~[cas-client-core-3.5.0.jar:3.5.0]
        at org.jasig.cas.client.validation.AbstractTicketValidationFilter.doFilter(AbstractTicketValidationFilter.java:204) ~[cas-client-core-3.5.0.jar:3.5.0]
    

![这里写图片描述](https://img-blog.csdn.net/20180208203019667?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWVsbGxvd2Nvbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

参考文章
----

[https://apereo.github.io/cas/5.2.x/protocol/REST-Protocol.html](https://apereo.github.io/cas/5.2.x/protocol/REST-Protocol.html)  
[http://makaidong.com/wggj/0/39620_9121845.html](http://makaidong.com/wggj/0/39620_9121845.html)
