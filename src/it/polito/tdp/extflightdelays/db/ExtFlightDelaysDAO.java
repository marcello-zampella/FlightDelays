package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports(HashMap<Integer,Airport> aereoporti) {
		String sql = "SELECT * FROM airports";
		List<Airport> result=new ArrayList<Airport>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				if(aereoporti.get(rs.getInt("ID"))==null) {
					Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
							rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
							rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
					aereoporti.put(airport.getId(), airport);
				}
				else {
					result.add(aereoporti.get(rs.getInt("ID")));
				}
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public List<Rotta> getRotte(Map<Integer,Airport> aIdMap, int distanzamedia) {
		String sql="SELECT ORIGIN_AIRPORT_ID as id1, DESTINATION_AIRPORT_ID as id2, AVG(DISTANCE) AS avgg " + 
				"FROM flights " + 
				"GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID " + 
				"HAVING avgg>? ;";
				List<Rotta> result= new ArrayList<Rotta>();
				
				try {
					Connection conn = DBConnect.getConnection();
					PreparedStatement st = conn.prepareStatement(sql);
					st.setInt(1, distanzamedia);
					ResultSet rs = st.executeQuery();

					while (rs.next()) {
						Airport partenza=aIdMap.get(rs.getInt("id1"));
						Airport destinazione=aIdMap.get(rs.getInt("id2"));
						Rotta rotta=new Rotta(partenza,destinazione, rs.getDouble("avgg"));
						result.add(rotta);
					}

					conn.close();
					return result;

				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Errore connessione al database");
					throw new RuntimeException("Error Connection Database");
				}
				
	}

}
