package io.java.coronavirustracker.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.java.coronavirustracker.models.LocationStats;

@Service
public class CoronaVirusDataService {

	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	
	private List<LocationStats> allStats = new ArrayList<>();

	public List<LocationStats> getAllStats() {
		return allStats;
	}
	
	//public static void main(String args[])
	//{
	//	String output = fetchVirusData(VIRUS_DATA_URL);
	//	System.out.println(output);
	//}
	
	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException,InterruptedException
	{
		List<LocationStats> newStats = new ArrayList<>();
		String line;
		StringBuilder content = new StringBuilder();
		
		try {
			URL url = new URL(VIRUS_DATA_URL);
			
			URLConnection urlConnection = url.openConnection();
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					
			while((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
				content.append(System.lineSeparator());
			}
			bufferedReader.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		StringReader csvBodyReader = new StringReader(content.toString());
		
		
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
			for (CSVRecord record : records) {
				LocationStats locationStat = new LocationStats();
			    locationStat.setState(record.get("Province/State"));
			    locationStat.setCountry(record.get("Country/Region"));
			    int latestCases = Integer.parseInt(record.get(record.size() - 1));
			    int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			    locationStat.setLatestTotalCases(latestCases);
			    locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
			    System.out.println(locationStat);
			    newStats.add(locationStat);
			}
			
			//to make it concurrency proof
			this.allStats = newStats;	
	}
}
