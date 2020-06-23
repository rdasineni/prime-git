package com.smpc.app.pil.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.stereotype.Repository;

import com.smpc.app.common.SSLDisableFix;

import gate.CorpusController;

@Repository
public class VistaarPilDAOImpl implements VistaarPilDAO {
	Document doc = null;
	private CorpusController corpusController;
	@Value("${vistaar.smpcUrl}")
	private String vspcUrl;

	@Value("${vistaar.userAgent}")
	private String vspcuserAgent;

	@Value("${vistaar.timeOut}")
	private String vspctimeOut;

	@Value("${vistaar.filePath}")
	private String vfilePath;

	@Value("${vistaar.fileType}")
	private String vfileType;

	@Value("${vistaar.filePdfType}")
	private String vfilePdfType;

	@Autowired
	private JdbcTemplate jdbcTemplateOne;

	@Autowired
	@Qualifier("firstDataSource")
	private DataSource firstDataSource;

	@Override
	public String getPilInfo() {
		// TODO Auto-generated method stub
		getUrlNumber();
		return "Pil data crawled successfully........";
	}

	public void getUrlNumber() {
		try {
			SSLDisableFix.execute();
			Document doc = Jsoup.connect(vspcUrl).followRedirects(true).referrer(vspcUrl).userAgent(vspcuserAgent)
					.timeout(Integer.parseInt(vspctimeOut)).get();
			System.out.println("Connect Url........");
			Elements div = doc.select("div[class=col-md-12 browse-head]");
			Elements list = div.select("li");
			int i = 1;
			for (Element sub : list) {
				String link = sub.select("a").attr("href");
				String[] alink = link.split("/emc/");
				String abslink = alink[1];
				System.out.println(abslink);
				if (i != 1) {
					getSubUrls1(abslink);
				}
				i++;
			}
			div.clear();
			list.clear();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getSubUrls1(String abslink) {
		try {
			String url1 = "https://www.medicines.org.uk/emc/" + abslink;
			String letter = abslink.replaceAll("browse-medicines/", "");
			Thread.sleep(2000);
			Document doc1 = Jsoup.connect(url1).followRedirects(true).referrer(url1).userAgent(vspcuserAgent)
					.timeout(Integer.parseInt(vspctimeOut)).get();
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
					System.out.println(url);
					getSubUrls(url);
				}
			}
			div.clear();
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
			Thread.sleep(2000);
			Document doc1 = Jsoup.connect(url1).followRedirects(true).referrer(url1).userAgent(vspcuserAgent)
					.timeout(Integer.parseInt(vspctimeOut)).get();
			Elements div = doc1.select("div[class=col-md-10 col-md-offset-1 data-results search-panel-results]");
			Elements div1 = div.select("div[class=row data-row]");
			Elements div2 = div1.select("div[class=col-sm-3]");
			Elements list = div2.select("li");
			for (Element sub1 : list) {
				String link = sub1.select("a").attr("abs:href");
				if (link.indexOf("/pil") != -1) {
					String pilNum = link.replaceAll("https://www.medicines.org.uk/emc/product/", "")
							.replaceAll("/pil", "").trim();
					getPdfUrls(link, pilNum);
				}

				// getPdfUrls(abslink1);
			}
			list.clear();
			div.clear();
			div1.clear();
			div2.clear();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getPdfUrls(String url, String pilNum) {
		try {
			Thread.sleep(2000);
			Document doc = Jsoup.connect(url).followRedirects(true).referrer(url).userAgent(vspcuserAgent)
					.timeout(Integer.parseInt(vspctimeOut)).get();
			Elements title = doc.select("div[class=container-fluid contracted content]");
			String uTitle = title.select("div[class=col-md-12 title]").text();
			String productName = uTitle.replaceAll("[/]", "");
			Elements date = doc.select("div[class=sidebar-module last-updated grey-border side-links no-mob]");
			Element span = date.select("h3").first();
			String updatedDate = span.select("span").text();
			Elements smpcs1 = doc.select("main[class=main-home]");
			Elements smpcs2 = smpcs1.select("div[class=xpilcontent]");
			if (smpcs2 != null && !smpcs2.isEmpty()) {
				String fPath = vfilePath + pilNum + vfileType;
				File f = new File(fPath);
				FileWriter fw = new FileWriter(f, true);
				BufferedWriter bw = new BufferedWriter(fw);
				for (Element subh : smpcs2) {
					String smpc2 = subh.html();
					bw.write(smpc2);
					bw.newLine();
					bw.flush();

				}
				bw.close();
				fw.close();
				insertLeafletInfo(url, productName, pilNum, fPath, updatedDate);
			} else {
				Elements docment = smpcs1.select("div[class=pil-download]");
				if (docment != null && !docment.isEmpty()) {
					String pdfPath = vfilePath + pilNum + vfilePdfType;
					String documentUrl = docment.select("a").attr("abs:href");
					saveDocument(documentUrl, pdfPath);
					insertLeafletInfo(documentUrl, productName, pilNum, pdfPath, updatedDate);
				}
			}
			smpcs1.clear();
			smpcs2.clear();
			title.clear();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveDocument(String documentUrl, String pdfPath) {
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(documentUrl);
			String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36";
			Thread.sleep(2000);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.addRequestProperty("User-Agent", userAgent);
			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setConnectTimeout(99999999);
			httpConn.setReadTimeout(99999999);
			in = httpConn.getInputStream();
			if (httpConn.getResponseCode() == 200) {
				File myFile = new File(pdfPath);
				fos = new FileOutputStream(myFile);
				int length = -1;
				byte[] buffer = new byte[1024];
				while ((length = in.read(buffer)) > -1) {
					fos.write(buffer, 0, length);
				}
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void insertLeafletInfo1(String documentUrl, String productName, String pilNum, String path,
			String updatedDate) {
		try {
			File image = new File(path);
			FileInputStream fis = new FileInputStream(image);
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(firstDataSource).withProcedureName("VISTAAR_SPC_LeafLets");
			SqlParameterSource in = new MapSqlParameterSource().addValue("I_SMC_URL", documentUrl)
					.addValue("IN_SPC_DOCUMENT", fis, (int) image.length()).addValue("I_PRODUCT_NAME", productName)
					.addValue("I_update_date", updatedDate).addValue("I_drugNum", pilNum);

			jdbcCall.execute(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertLeafletInfo(String documentUrl, String productName, String pilNum, String path,
			String updatedDate) {
		File image = null;
		FileInputStream fis = null;
		try {
			image = new File(path);
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(firstDataSource).withProcedureName("VISTAAR_SPC_LeafLets")
					.declareParameters(new SqlParameter("I_SMC_URL", Types.VARCHAR),
							new SqlParameter("IN_SPC_DOCUMENT", Types.BLOB),
							new SqlParameter("I_PRODUCT_NAME", Types.VARCHAR),
							new SqlParameter("I_update_date", Types.VARCHAR),
							new SqlParameter("I_drugNum", Types.VARCHAR));
			if (image.exists()) {
				fis = new FileInputStream(image);
				MapSqlParameterSource in = new MapSqlParameterSource().addValue("I_SMC_URL", documentUrl)
						.addValue("IN_SPC_DOCUMENT", new SqlLobValue(fis, (int) image.length()))
						.addValue("I_PRODUCT_NAME", productName).addValue("I_update_date", updatedDate)
						.addValue("I_drugNum", pilNum);
				jdbcCall.execute(in);
				jdbcCall = null;
				fis.close();
				if (image.exists())
					image.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * @Override public String getManfacturerInfo() { // TODO Auto-generated method
	 * stub initializeGate(); loadGateApplication(); int doc=getDocumentsCount();
	 * System.out.println("Documents count : "+doc); findAll(); return null; }
	 * 
	 * private void initializeGate() { try { // Initializing Gate Gate.init(); }
	 * catch (Exception exception) { exception.printStackTrace(); } }
	 * 
	 * public void loadGateApplication() { try { corpusController =
	 * (CorpusController) PersistenceManager.loadObjectFromFile(new
	 * File("D:\\Spring-Boof-Config\\vistaar-smpc\\gate-files\\application.xgapp"));
	 * 
	 * } catch (Exception exception) { exception.printStackTrace(); } }
	 * 
	 * public int getDocumentsCount() { int count = 0; try { count =
	 * jdbcTemplateOne.
	 * queryForObject("Select count(*) from SMPC_LEAFLET where status=0 and MANUFACTURER_name is null and urls not like '%/pil"
	 * ,int.class); } catch (Exception exception) { exception.printStackTrace(); }
	 * return count; }
	 * 
	 * public void findAll() {
	 * 
	 * String sql =
	 * "select id,product_name,urls from SMPC_DOCUMENTS where id in(301,302,303)";
	 * 
	 * List<VistaalPil> customers = jdbcTemplateOne.query(sql,new
	 * BeanPropertyRowMapper(VistaalPil.class));
	 * 
	 * }
	 */

}
