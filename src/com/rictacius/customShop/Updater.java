package com.rictacius.customShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;

public class Updater {
	public static String newVersion;

	public Updater() {
	}

	public static boolean check() {
		try {
			URL url = new URL("http://rictacius.bplaced.net/pages/plugins/download/");

			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("CustomShop")) {
					String version = line.split(":")[1];
					if (!version.equals(Main.pl.getDescription().getVersion())) {
						newVersion = version;
						return true;
					}
				}
			}
		} catch (Exception e) {
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Could not check for updates!"), ChatColor.RED);
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		return false;
	}

	public static boolean update() {
		try {
			URL website = new URL("http://rictacius.bplaced.net/pages/plugins/download/CustomShop/" + newVersion
					+ "/CustomShop" + Shops.version + ".jar");
			FileUtils.copyURLToFile(website, new File("plugins/CustomShop" + Shops.version + ".jar"));
			return true;
		} catch (Exception e) {
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Could not download update (v" + newVersion + ")"),
					ChatColor.RED);
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		return false;
	}

}
