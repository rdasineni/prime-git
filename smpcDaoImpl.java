package com.smpc.app.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.sql.Types;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;

import com.smpc.app.common.SSLDisableFix;

@Repository
public class VistaarSmpcDaoImpl implements VistaarSmpcDAO {
	private static final Logger logger = LoggerFactory.getLogger(VistaarSmpcDaoImpl.class);
	Document doc = null;
	@Value("${vistaar.smpcUrl}")
	private String vspcUrl;

	@Value("${vistaar.userAgent}")
	private String vspcuserAgent;

	@Value("${vistaar.timeOut}")
	private String vspctimeOut;
	@Autowired
	@Qualifier("firstDataSource")
	private DataSource firstDataSource;

	@Override
	public String getDrugInfo() {
		// TODO Auto-generated method stub
		logger.info("Started smpc documents crawlling from emc website...");
		getUrlNumber();
		return "Crawled smpc urls successfully........";
	}

	public String getSmpcUrls() {
		try {

		} catch (Exception e) {

			e.printStackTrace();
		}
		return "Crawling smpc url successfully...............";
	}

	public void getUrlNumber() {
		try {
			Thread.sleep(3000);
			Document doc = Jsoup.connect(vspcUrl).followRedirects(true).referrer(vspcUrl).userAgent(vspcuserAgent)
					.timeout(10*Integer.parseInt(vspctimeOut)).get();
			System.out.println("Connect Url........");
			Elements div = doc.select("div[class=col-md-12 browse-head]");
			Elements list = div.select("li");
			int i = 1;
			for (Element sub : list) {
				String link = sub.select("a").attr("href");
				String[] alink = link.split("/emc/");
				String abslink = alink[1];
				System.out.println("111111111111111111" + abslink);
				if (i != 1) {
					getSubUrls1(abslink);
				}
				i++;
			}
		} catch (Exception e) {
			logger.error("Exception found while getting urls" + e);
			e.printStackTrace();
		}
	}

	public void getSubUrls1(String abslink) {
		try {
			String url1 = "https://www.medicines.org.uk/emc/" + abslink;
			String letter = abslink.replaceAll("browse-medicines/", "");
			Thread.sleep(3000);
			Document doc1 = Jsoup.connect(url1).followRedirects(true).referrer(url1).userAgent(vspcuserAgent)
					.timeout(10*Integer.parseInt(vspctimeOut)).get();
			Elements div = doc1.select("span[class=search-paging-view]");
			String divtext = div.text();
			if (divtext.indexOf("results found") != -1) {
				int pos = divtext.indexOf("results found");
				String count = divtext.substring(0, pos).trim();
				int fcount = Integer.parseInt(count);
				int tempcount = 1;
				while (tempcount <= fcount) {
					String url = "https://www.medicines.org.uk/emc/browse-medicines?prefix=" + letter + "&offset="
							+ tempcount + "&limit=200";
					tempcount = tempcount + 200;
					System.out.println("222222222222 " + url);
					getSubUrls(url);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getSubUrls(String url1) {
		try {
			Thread.sleep(3000);
			Document doc1 = Jsoup.connect(url1).followRedirects(true).referrer(url1).userAgent(vspcuserAgent)
					.timeout(10*Integer.parseInt(vspctimeOut)).get();
			Elements div = doc1.select("div[class=col-md-10 col-md-offset-1 data-results search-panel-results]");
			Elements div1 = div.select("div[class=row data-row]");
			Elements div2 = div1.select("div[class=col-sm-9]");
			Elements list = div2.select("h2");
			for (Element sub1 : list) {
				String link = sub1.select("a").attr("href");
				String[] alink = link.split("/emc/");
				String abslink1 = alink[1];
				getSubUrlsdata(abslink1);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getSubUrlsdata(String abslink1) {
		try {
			Thread.sleep(3000);
			String url2 = "https://www.medicines.org.uk/emc/" + abslink1;
			URLEncoder.encode(url2, "UTF-8") ;
			Document doc2 = Jsoup.connect(url2).followRedirects(true).referrer(url2).userAgent(vspcuserAgent)
					.timeout(10*Integer.parseInt(vspctimeOut)).get();
			Elements title = doc2.select("div[class=container-fluid contracted content]");
			String title1 = title.select("div[class=col-md-12 title]").text();
			Elements titlerow = doc2.select("div[class=row detail]");
			String actin = titlerow.select("div[class=col-xs-12 col-sm-6]").first().text()
					.replaceAll("Active ingredient", "").trim();
			System.out.println("title:-------------> " + actin);
			Elements smpcs1 = doc2.select("div[class=col-md-9 col-md-pull-3 col-lg-8 col-lg-pull-4 tab-inner]");
			String smpc2 = smpcs1.html();
			// String manInfo=getSubPLeafLey(abslink1);
			// smpc2=smpc2+"\n"+manInfo;
			String html = "<html><body>";
			String html1 = "</body></html>";
			String updateDate = getEmcupdateDate(abslink1);
			String drugNum = url2.replaceAll("https://www.medicines.org.uk/emc/product/", "").replaceAll("/smpc", "")
					.replaceAll("/pil", "").trim();
			insertspcurl(url2, title1, html + smpc2 + html1, updateDate, drugNum, actin);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getEmcupdateDate(String abslink1) {
		String updateDate = "";
		try {
			SSLDisableFix.execute();
			Thread.sleep(2000);
			String url2 = "https://www.medicines.org.uk/emc/" + abslink1;
			Document doc2 = Jsoup.connect(url2).followRedirects(true).referrer(url2).userAgent(vspcuserAgent)
					.timeout(10 * 10000).get();
			Elements title = doc2.select("div[class=sidebar-module last-updated grey-border side-links no-mob]");
			updateDate = title.select("h3").text().replaceAll("Last updated on emc:", "").trim();
			System.out.println(updateDate);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return updateDate;
	}

	public void insertspcurl(String url, String title, String smpcdoc, String updateDate, String drugNum,
			String actin) {
		try {
			// System.out.println(url+" -- "+title+" -- "+smpcdoc+" -- "+updateDate+" --
			// "+drugNum);

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(firstDataSource).withProcedureName("VISTAAR_SPC_DOCUMENTS");
			SqlParameterSource in = new MapSqlParameterSource().addValue("I_SMC_URL", url)
					// .addValue("IN_SPC_DOCUMENT", smpcdoc.getBytes())
					.addValue("IN_SPC_DOCUMENT",
							new SqlLobValue(new ByteArrayInputStream(smpcdoc.getBytes()), smpcdoc.getBytes().length,
									new DefaultLobHandler()),
							Types.BLOB)
					.addValue("IN_TITLE", title).addValue("I_update_date", updateDate).addValue("I_drugNum", drugNum)
					.addValue("I_act_ingredient", actin);
			jdbcCall.execute(in);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}