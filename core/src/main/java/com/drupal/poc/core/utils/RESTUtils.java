package com.drupal.poc.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.drupal.poc.dto.ArticlesDTO;
import com.drupal.poc.dto.BaseDTO;
import com.drupal.poc.dto.BasicDTO;
import com.drupal.poc.dto.RecipesDTO;
import com.drupal.poc.dto.ValueDTO;
import com.google.gson.Gson;

public class RESTUtils {
	private static final String HOST = "http://localhost:80";
	private static final String API = "/drupal8/en/rest/v1/";
	private static final String FORMAT = "?_format=json";

	private static HttpURLConnection createConnection(String connectionURI) {
		URL url;
		HttpURLConnection connection = null;
		try {
			url = new URL(connectionURI);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ connection.getResponseCode());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}

	private static String getContentString(HttpURLConnection connection) {
		StringBuffer sb = new StringBuffer();
		Charset UTF_8 = Charset.forName("UTF-8");
		Charset ISO = Charset.forName("ISO-8859-1");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(connection.getInputStream()), "UTF-8"));
			System.out.println("Output from Server .... \n");
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(new String(output.getBytes(ISO), UTF_8));
			}
			connection.disconnect();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return sb.toString();
	}

	private static Object[] processContent(String content, String type) {
		Gson gson = new Gson();
		BaseDTO<Object> baseDTO = new BaseDTO<Object>();
		switch (type) {
		case "articles":
			ArticlesDTO[] articles = gson
					.fromJson(content, ArticlesDTO[].class);
			baseDTO.setDtoList(articles);
			break;
		case "recipes":
			RecipesDTO[] recipes = gson.fromJson(content, RecipesDTO[].class);
			baseDTO.setDtoList(recipes);
			break;
		case "basicpages":
			BasicDTO[] basicpages = gson.fromJson(content, BasicDTO[].class);
			baseDTO.setDtoList(basicpages);
			break;
		default:
			// code block
		}

		return baseDTO.getDtoList();

	}

	private static void updateArticleComponentProperties(
			ArticlesDTO articlesDTO, Node articleNode, String pageTitle) {
		String[] tags = new String[articlesDTO.getTags().length];
		int counter = 0;
		for (ValueDTO tag : articlesDTO.getTags()) {
			tags[counter] = tag.getUrl().replace("/drupal8/en/tags/", "");
			counter++;
		}
		try {
			articleNode.setProperty("sling:resourceType",
					"drupalpocsite/components/content/drupal-article");
			articleNode.setProperty("title", pageTitle);
			articleNode
					.setProperty("body", articlesDTO.getBody()[0].getValue());
			articleNode
					.setProperty("image", articlesDTO.getImage()[0].getUrl());

			if (tags.length > 1) {
				articleNode.setProperty("tags", tags);
			} else {
				articleNode.setProperty("tags", tags[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateBasicComponentProperties(BasicDTO basicDTO,
			Node basicNode, String pageTitle) {

		try {
			basicNode.setProperty("sling:resourceType",
					"drupalpocsite/components/content/drupal-article");
			basicNode.setProperty("title", pageTitle);
			basicNode.setProperty("body", basicDTO.getBody()[0].getValue());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateRecipesComponentProperties(RecipesDTO recipesDTO,
			Node recipesNode, String pageTitle) {
		String[] tags = new String[recipesDTO.getField_tags().length];
		String[] ingredients = new String[recipesDTO.getField_ingredients().length];
		int counter = 0;
		for (ValueDTO tag : recipesDTO.getField_tags()) {
			tags[counter] = tag.getUrl().replace("/drupal8/en/tags/", "");
			counter++;
		}
		counter = 0;
		for (ValueDTO ingredient : recipesDTO.getField_ingredients()) {
			ingredients[counter] = ingredient.getValue();
			counter++;
		}
		try {
			recipesNode.setProperty("sling:resourceType",
					"drupalpocsite/components/content/drupal-recipes");
			recipesNode.setProperty("title", pageTitle);
			recipesNode.setProperty("cookingTime",
					recipesDTO.getField_cooking_time()[0].getValue());
			recipesNode.setProperty("difficulty",
					recipesDTO.getField_difficulty()[0].getValue());
			recipesNode.setProperty("ingredients", ingredients);
			recipesNode.setProperty("noOfServings",
					recipesDTO.getField_number_of_servings()[0].getValue());
			recipesNode.setProperty("preparationTime",
					recipesDTO.getField_preparation_time()[0].getValue());
			recipesNode.setProperty(
					"category",
					recipesDTO.getField_recipe_category()[0].getUrl()
							.substring(
									recipesDTO.getField_recipe_category()[0]
											.getUrl().lastIndexOf("/") + 1,
									recipesDTO.getField_recipe_category()[0]
											.getUrl().length()));
			recipesNode.setProperty("instruction",
					recipesDTO.getField_recipe_instruction()[0].getValue());
			recipesNode.setProperty("summary",
					recipesDTO.getField_summary()[0].getValue());
			recipesNode.setProperty("image",
					recipesDTO.getField_media_image()[0].getUrl());
			if (tags.length > 1) {
				recipesNode.setProperty("tags", tags);
			} else {
				recipesNode.setProperty("tags", tags[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> createConent(SlingHttpServletRequest slingRequest)
			throws WCMException, PathNotFoundException, RepositoryException {
		String templatePath = "/apps/drupalpocsite/templates/page-content";
		Page newPage;
		PageManager pageManager;
		List<String> pageList = new ArrayList<String>();
		String type = slingRequest.getParameter("type").toString();
		String path = slingRequest.getParameter("path").toString();
		HttpURLConnection connection = RESTUtils.createConnection(HOST + API
				+ type + FORMAT);

		String content = RESTUtils.getContentString(connection);
		Object[] objectArray = RESTUtils.processContent(content, type);

		ResourceResolver resolver = slingRequest.getResourceResolver();

		Session session = resolver.adaptTo(Session.class);

		// create a page manager instance
		pageManager = resolver.adaptTo(PageManager.class);
		Resource resource = slingRequest.getResourceResolver()
				.getResource(path);
		Page page = resource.adaptTo(Page.class);
		for (Object obj : objectArray) {
			ArticlesDTO articlesDTO = null;
			RecipesDTO recipesDTO = null;
			BasicDTO basicDTO = null;
			ValueDTO pagepath = null;
			String pageTitle = null;
			switch (type) {
			case "articles":
				articlesDTO = (ArticlesDTO) obj;
				pagepath = articlesDTO.getPath()[0];
				pageTitle = articlesDTO.getTitle()[0].getValue();
				break;
			case "recipes":
				recipesDTO = (RecipesDTO) obj;
				pagepath = recipesDTO.getPath()[0];
				pageTitle = recipesDTO.getTitle()[0].getValue();
				break;
			case "basicpages":
				basicDTO = (BasicDTO) obj;
				pagepath = basicDTO.getPath()[0];
				pageTitle = basicDTO.getTitle()[0].getValue();
				break;

			}

			String pageName = pagepath.getAlias().substring(
					pagepath.getAlias().lastIndexOf("/") + 1,
					pagepath.getAlias().length());
			newPage = pageManager.create(path, pageName, templatePath,
					pageTitle);

			pageList.add(newPage.getPath());
			if (newPage != null) {
				Node newNode = newPage.adaptTo(Node.class);
				Node cont = newNode.getNode("jcr:content");
				if (cont != null) {
					Node rootNode = session.getRootNode();
					Node node = JcrUtils.getOrCreateByPath(cont.getPath() + "/"
							+ type, JcrConstants.NT_UNSTRUCTURED, session);
					switch (type) {
					case "articles":
						RESTUtils.updateArticleComponentProperties(articlesDTO,
								node, pageTitle);
						break;
					case "recipes":
						RESTUtils.updateRecipesComponentProperties(recipesDTO,
								node, pageTitle);
						break;
					case "basicpages":
						RESTUtils.updateBasicComponentProperties(basicDTO,
								node, pageTitle);
						break;
					default:
						// code block

					}
				}
			}
			session.save();

		}

		return pageList;
	}

}
