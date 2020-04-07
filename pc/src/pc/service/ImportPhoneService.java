package pc.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pc.exception.UserDefineException;

public class ImportPhoneService {

	private static final Logger logger = LoggerFactory.getLogger(ImportPhoneService.class);
	
	public static ArrayList<String> readFromFile(File f) throws UserDefineException {
		ArrayList<String> result = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = br.readLine()) != null) {
				result.add(line);
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new UserDefineException("File not exists.", e);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new UserDefineException("File error", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
