package com.rictacius.customShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Updater {
	public static String newVersion;
	public static boolean devBuild;

	public Updater() {
	}

	public static boolean check() {
		newVersion = null;
		try {
			URL url = new URL("http://rictacius.bplaced.net/pages/plugins/download/");

			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("CustomShop")) {
					String version = line.split(":")[1];
					if (!version.contains(".")) {
						if (!version.equals(Main.pl.getDescription().getVersion())) {
							newVersion = version;
							return true;
						}
					} else {
						String[] newVersionParts = version.split(".");
						String[] currentVersionParts = Main.pl.getDescription().getVersion().split(".");
						if (newVersionParts.length != currentVersionParts.length) {
							newVersion = version;
							return true;
						} else {
							for (int i = 0; i < newVersionParts.length; i++) {
								int a = Integer.parseInt(newVersionParts[i]);
								int b = Integer.parseInt(currentVersionParts[i]);
								if (a >= b) {
									devBuild = false;
									if (a > b) {
										newVersion = version;
										return true;
									}
								} else {
									devBuild = true;
									break;
								}
							}
						}
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

	public static boolean download() {
		try {
			URL website = new URL("http://rictacius.bplaced.net/pages/plugins/download/CustomShop/" + newVersion
					+ "/CustomShop" + getVersion() + ".jar");
			File toSave = new File("plugins/CustomShop" + getVersion("") + ".jar");
			FileUtils.copyURLToFile(website, toSave, 30000, 30000);
		} catch (Exception e) {
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Could not download update (v" + newVersion + ")"),
					ChatColor.RED);
			Methods.sendColoredMessage(Main.pl, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static String getVersion() {
		return "";
	}

	private static String getVersion(String s) {
		String version = Bukkit.getServer().getVersion();
		int svindex = version.indexOf('(');
		version = version.substring(svindex);
		version = version.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("MC:", "");
		String[] data = version.split("\\.");
		version = data[0] + "." + data[1];
		version = version.trim();
		return version;
	}

}
