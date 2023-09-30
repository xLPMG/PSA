package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import util.AES;

public class DataHandler {

	// HashMap<ProgramName, username:password>
	String os = "win";

	private HashMap<String, String> dataMap = new HashMap<>();
	private File psaFolder;
	private File dataStorage;
	AES aes;
	private String keycode;
	private String generalPassword = "";
	private String hint = "";

	public DataHandler() {
		os = System.getProperty("os.name").toLowerCase();
		init();
		loadData();
	}

	private void init() {
		if (os.contains("win")) {
			psaFolder = new File(System.getenv("APPDATA") + "/PSA");
			if (!psaFolder.exists()) {
				psaFolder.mkdirs();
			}
			dataStorage = new File(System.getenv("APPDATA") + "/PSA/psadata.dat");
			if (!dataStorage.exists()) {
				try {
					dataStorage.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (os.contains("mac")) {
			psaFolder = new File(System.getProperty("user.home", "."), "Library/Application Support/" + "PSA");
			if (!psaFolder.exists()) {
				psaFolder.mkdirs();
			}
			dataStorage = new File(System.getProperty("user.home", "."), "Library/Application Support/PSA/psadata.dat");
			if (!dataStorage.exists()) {
				try {
					dataStorage.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		aes = new AES();
		try {
			keycode = getKeycode();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getKeycode() throws IOException {
		String salt = "u8IkY=Tnij?S";

		if (os.contains("win")) {
			String command = "wmic csproduct get UUID";
			StringBuffer output = new StringBuffer();

			Process SerNumProcess = Runtime.getRuntime().exec(command);
			BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

			String line = "";
			while ((line = sNumReader.readLine()) != null) {
				output.append(line + "\n");
			}
			String MachineID = output.toString().substring(output.indexOf("\n"), output.length()).trim();
			try {
				SerNumProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return MachineID + salt;
		} else if (os.contains("mac")) {
			String command = "system_profiler SPHardwareDataType | awk '/UUID/ { print $3; }'";
			StringBuffer output = new StringBuffer();

			Process SerNumProcess = Runtime.getRuntime().exec(command);
			BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

			String line = "";
			while ((line = sNumReader.readLine()) != null) {
				output.append(line + "\n");
			}

			String MachineID = output.toString().substring(output.indexOf("UUID: "), output.length()).replace("UUID: ",
					"");
			try {
				SerNumProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sNumReader.close();
			return MachineID + salt;
		}
		return salt + salt + salt;

	}

	private void loadData() {
		try {
			dataMap.clear();
			BufferedReader br = new BufferedReader(new FileReader(dataStorage.getAbsolutePath()));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String decodedLine = aes.decode(line, keycode);
					if (decodedLine.startsWith("pass:")) {
						generalPassword = decodedLine.split(":")[1];
					} else if (decodedLine.startsWith("hint:")) {
						hint = decodedLine.split(":")[1];
						hint = hint.replace("x3", "");
					} else {
						String data[] = decodedLine.split("[,]");
						dataMap.put(data[0], data[1]);
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveData() {
		try {
			FileWriter writer = new FileWriter(dataStorage.getAbsolutePath());
			writer.write("");
			for (Entry<String, String> entry : dataMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				writer.write(aes.encode(key + "," + value, keycode));
				writer.write(System.getProperty("line.separator"));
			}
			writer.write(aes.encode("pass:" + generalPassword, keycode));
			writer.write(System.getProperty("line.separator"));
			writer.write(aes.encode("hint:" + hint + "x3", keycode));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadData();
	}

	public String getPassword() {
		loadData();
		return generalPassword;
	}

	public void setPassword(String generalPassword) {
		this.generalPassword = generalPassword;
		saveData();
	}

	public void setPassword(String generalPassword, String hint) {
		this.generalPassword = generalPassword;
		this.hint = hint;
		saveData();
	}

	public String getHint() {
		loadData();
		return hint;
	}

	public HashMap<String, String> getData() {
		return dataMap;
	}

	public void setData(HashMap<String, String> dataMap) {
		this.dataMap = dataMap;
		saveData();
	}

	public void addProgram(String programName, String username, String password) {
		dataMap.put(programName, username + ":" + password);
		saveData();
	}

	public void removeProgram(String programName) {
		if (dataMap.containsKey(programName)) {
			dataMap.remove(programName);
			saveData();
		}
	}
}
