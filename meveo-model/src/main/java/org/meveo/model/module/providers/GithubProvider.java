/**
 * 
 */
package org.meveo.model.module.providers;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.meveo.model.module.InstallableModule;
import org.meveo.model.module.MeveoModuleProvider;
import org.meveo.model.persistence.JacksonUtil;

public class GithubProvider implements MeveoModuleProviderInterface {
	private MeveoModuleProvider provider;
	
	public void init(MeveoModuleProvider provider) {
		this.provider = provider;
	}
	
	private InstallableModule fromMap(Map<String, Object> map) {
		String defaultBranch = (String) map.get("default_branch");
		String repoName = (String) map.get("name");
		InstallableModule module = new InstallableModule();
		module.setUrl((String) map.get("html_url"));
		module.setProvider(provider);
		String moduleJsonUrl =  "https://raw.githubusercontent.com/" + 
				provider.getProviderName() + "/" + 
				repoName + "/" + 
				defaultBranch + "/module.json";
		
		HttpURLConnection con = null;
		try {
			// Get code, description, version 
			con = (HttpURLConnection) new URL(moduleJsonUrl).openConnection();
			con.setRequestMethod("GET");
			if (provider.getAccessToken() != null) {
				con.setRequestProperty("Authorization", "Bearer " + provider.getAccessToken());
			}
			Map<String, Object> moduleDescriptor = JacksonUtil.toMap(con.getInputStream());
			module.completeFromModuleDescriptor(moduleDescriptor);
			
			// Get commit
			con.disconnect();
			con = (HttpURLConnection) new URL("https://api.github.com/repos/" + provider.getProviderName() + "/" + repoName + "/commits" )
					.openConnection();
			Map<String, Object> latestCommit = JacksonUtil.toList(con.getInputStream()).get(0);
			module.setCommitSha((String) latestCommit.get("sha"));
			return module;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}
	
	public List<InstallableModule> list() throws Exception {
		int count = -1;
		int pageCount = 0;
		List<InstallableModule> modules = new ArrayList<>();
		while (count != 0) {
			URL url = new URL("https://api.github.com/orgs/" + provider.getProviderName() + "/repos?page=" + pageCount);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			if (provider.getAccessToken() != null) {
				con.setRequestProperty("Authorization", "Bearer " + provider.getAccessToken());
			}
			try {
				var repoList = JacksonUtil.toList(con.getInputStream());
				count = repoList.size();
				pageCount ++;
				modules.addAll(repoList.stream()
						.map(this::fromMap)
						.filter(Objects::nonNull)
						.collect(Collectors.toList()));
			} finally {
				con.disconnect();
			}
		}
		return modules;
	}
}
