package cc.rosickytong;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class App {

	public static void main(String[] args) throws IOException {
		List<String> dirs = Arrays
				.asList("./secure1", "./secure2", "./secure3");

		// Mar 27 10:08:26 VM_154_116_centos sshd[26949]: Failed password for
		// root from 222.45.87.22 port 39474 ssh2

		List<String> failed_password_lines = new ArrayList<String>(100000);

		for (String dir : dirs) {
			File folder = new File(dir);
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				Path path = file.toPath();
				List<String> lines = Files.readAllLines(path,
						Charset.defaultCharset());
				for (String line : lines) {
					if (StringUtils.contains(line, "Failed password")) {
						failed_password_lines.add(line);
					}
				}
			}
		}

		Map<String, Integer> filters = new HashMap<String, Integer>(1000);

		for (String line : failed_password_lines) {
			String ip = StringUtils.substringBetween(line, "from", "port")
					.trim();
			if (ip != null && !ip.isEmpty()) {
				Integer count = filters.get(ip);
				if (count == null) {
					count = 0;
				}
				count += 1;
				filters.put(ip, count);
			}
		}

		for (Entry<String, Integer> entry : filters.entrySet()) {
			String ip = entry.getKey();
			Integer count = entry.getValue();
			if (count > 3) {
				System.out.println("iptables -I INPUT -s " + ip + " -j DROP");
			}
		}
		System.out.println("service iptables save");
	}
}
