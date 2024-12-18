package com.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private static final String DB_URL = "jdbc:mysql://192.168.1.5:3306/web_crawler";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    // 目标网站列表
    private static final List<String> TARGET_SITES = Arrays.asList(
/*       "https://www.bilibili.com",
        "https://www.baidu.com",
        "https://www.taobao.com",
        "https://www.jd.com",
        "https://www.weibo.com",
        "https://www.zhihu.com",
        "https://www.qq.com",
        "https://www.sohu.com",
        "https://www.sina.com.cn",
        "https://www.360.cn",
        "https://www.youku.com",
        "https://www.tencent.com",
        "https://www.alipay.com",
        "https://www.meituan.com",
        "https://www.douyin.com",
        "https://www.pinduoduo.com",
        "https://www.163.com",
        "https://www.ifeng.com",
        "https://www.58.com",
        "https://www.ctrip.com",
        "https://www.dianping.com",
        "https://www.tmall.com",
        "https://www.kuaishou.com",
        "https://www.xiaohongshu.com",
        "https://www.csdn.net",
        https://www.163.com
        "https://www.ifeng.com",
        "https://www.douban.com",
        "https://www.tianya.cn",
        "https://www.58.com",
        "https://www.ganji.com",
        "https://www.autohome.com.cn",
        "https://www.ctrip.com",
        "https://www.dianping.com",
        "https://www.ele.me",
        "https://www.baidu.com/more/",
        "https://www.hupu.com",
        "https://www.zol.com.cn",
        "https://www.smzdm.com",
        "https://www.yy.com",
        "https://www.ximalaya.com",
        "https://www.kugou.com",
        "https://www.csdn.net",
        "https://www.lagou.com",
        "https://www.jobui.com",
        "https://www.zhaopin.com",
        "https://www.liepin.com",
        "https://www.chinahr.com",
        "https://www.dajie.com",
        "https://www.w3school.com.cn",
        "https://www.runoob.com",
        "https://www.imooc.com",
        "https://www.xuetangx.com",
        "https://www.icourse163.org",
        "https://www.edx.org.cn",
        "https://www.bytedance.com",
        "https://www.toutiao.com",
        "https://www.douyin.com",
        "https://www.kuaishou.com",
        "https://www.acfun.cn",
        "https://www.miguvideo.com",
        "https://www.pptv.com",
        "https://www.letv.com",
        "https://www.wasu.cn",
        "https://www.fun.tv",
        "https://www.1905.com",
        "https://www.cntv.cn",
        "https://www.cctv.com",
        "https://www.people.com.cn",
        "https://www.xinhuanet.com",
        "https://www.gmw.cn",
        "https://www.youth.cn",
        "https://www.china.com.cn",
        "https://www.china.org.cn",
        "https://www.cnki.net",
        "https://www.wanfangdata.com.cn",
        "https://www.docin.com",
        "https://www.book118.com",
        "https://www.xueshu.com",
        "https://www.sciencenet.cn",
        "https://www.medsci.cn",
        "https://www.dxy.cn",
        "https://www.cn-healthcare.com",
        "https://www.99.com.cn",
        "https://www.120ask.com",
        "https://www.yaolan.com",
        "https://www.babytree.com",
        "https://www.mama.cn",
        "https://www.pcbaby.com.cn",
        "https://www.39.net",
        "https://www.xywy.com",
        "https://www.haodf.com",
        "https://www.guahao.com",
        "https://www.114yygh.com",
        "https://www.jkzdw.com",
        "https://www.aliyun.com",
        "https://www.tencentcloud.com",
        "https://www.huaweicloud.com",
        "https://www.qiniu.com",
        "https://www.upyun.com",
        "https://www.ucloud.cn",
        "https://www.sinacloud.com",
        "https://www.jcloud.com",
        "https://www.ksyun.com",
        "https://www.qingcloud.com",
        "https://www.vultr.com",
        "https://www.digitalocean.com",
        "https://www.linode.com",
        "https://www.aws.cn",
        "https://www.azure.cn",
        "https://www.google.cn",
        "https://www.bing.com",*/
        "https://www.jit.edu.cn",
        "https://www.baidu.com/"
    );

    private Set<String> visitedUrls = new HashSet<>();
    private int totalPagesCrawled = 0; // 全局计数器   记录已爬取的页面数量

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        crawler.crawlHomePages(); // 仅爬取目标网站的首页和前 5 个分页的 HTML
    }

    //与数据库建立连接，利用url遍历所有sites，调用crawlhomepage爬取网页并重置计数器
    public void crawlHomePages() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("与本地数据库连接成功！");

            //遍历所有sites
            for (String url : TARGET_SITES) {
                System.out.println("Crawling: " + url);
                crawlHomePage(url, connection, 5); // 爬取首页和前 5 个分页的 HTML

                // 如果已经爬取了 6 个页面，停止爬取当前网站，继续下一个网站
                if (totalPagesCrawled >= 6) {
                    System.out.println("已爬取 6 个页面，停止爬取当前网站，继续下一个网站。");
                    totalPagesCrawled = 0;        // 重置计数器
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    //先处理特殊情况，判断是否爬过该网站
    //使用httpconnect 连接服务器，获取网站头部信息并限制其内容类型
    //利用Jsoup爬取内容并保存至数据库
    private void crawlHomePage(String url, Connection connection, int maxPages) throws IOException, SQLException {
        if (visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);
        System.out.println("Crawling: " + url);

        // 使用 HttpURLConnection 检查 MIME 类型
        URL urlObj = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) urlObj.openConnection();
        httpConnection.setRequestMethod("HEAD"); // 只获取头部信息
        String contentType = httpConnection.getContentType();

        // 检查 contentType 是否为 null
        if (contentType == null) {
            System.out.println("Skipping URL with null contentType: " + url);
            return;
        }

        // 只处理 Jsoup 支持的 MIME 类型
        if (!contentType.startsWith("text/") && !contentType.contains("application/xml")) {
            System.out.println("跳过非文本信息: " + url);
            return;
        }

        // 使用 Jsoup 爬取首页内容
        Document document = Jsoup.connect(url).get();
        String title = document.title();
        String content = document.html(); // 获取 HTML 内容

        // 截断 HTML 内容以避免数据截断错误
        if (content.length() > 10000) { // 假设数据库列的最大长度为 10000
            content = content.substring(0, 10000);
        }

        saveToDatabase(connection, url, title, content);

        // 更新全局计数器
        totalPagesCrawled++;

        // 如果已经爬取了 6 个页面，停止爬取
        if (totalPagesCrawled >= 6) {
            System.out.println("已爬取 6 个页面，停止爬取当前网站。");
            return;
        }
/*<----------------------------------------------------------------------------------->*/    //分页的实现
        // 提取分页链接并爬取前 5 个分页的 HTML
        Elements subLinks = document.select("a[href]"); // 提取所有链接
        //这里面的 [herf]是一个css选择器，表示选择所有带有 href 属性的 <a> 标签
        //<a>标签就是超链接标签
        int pageCount = 0;
        for (Element link : subLinks) {
            String nextUrl = link.absUrl("href");

            // 检查链接是否为有效网页链接
            if (!nextUrl.startsWith("http") || visitedUrls.contains(nextUrl)) {
                continue;
            }

            // 只爬取前 5 个分页
            if (pageCount < maxPages) {
                crawlHomePage(nextUrl, connection, maxPages);
                pageCount++;
            } else {
                break;
            }
        }
    }

    private void saveToDatabase(Connection connection, String url, String title, String content) throws SQLException {
        String sql = "INSERT INTO web_pages (url, title, content) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE title=VALUES(title), content=VALUES(content)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, url);
            statement.setString(2, title);
            statement.setString(3, content);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("数据插入成功: " + url);
            } else {
                System.out.println("数据插入失败: " + url);
            }
        }
    }
}