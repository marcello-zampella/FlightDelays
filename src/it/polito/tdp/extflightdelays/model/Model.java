package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	HashMap<Integer,Airport> aIdMap;
	Map<Airport,Airport> visita;
	
	public Model() {
		grafo=new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		aIdMap=new HashMap<Integer,Airport>();
		visita= new HashMap<Airport,Airport>();
	}
	
	public void creaGrafo(int distanzaMedia) {
		ExtFlightDelaysDAO dao= new ExtFlightDelaysDAO();
		dao.loadAllAirports(aIdMap);
		
		Graphs.addAllVertices(grafo, aIdMap.values());
		
		for(Rotta rotta : dao.getRotte(aIdMap, distanzaMedia)) {
			//controllo se esiste già un arco tra i 2, perchè magari io ho partenza -> destinazione, ma esiste
			//già destinazione -> partenza, quindi se esiste aggiorno il peso
			DefaultWeightedEdge edge= grafo.getEdge(rotta.getPartenza(), rotta.getDestinazione());
			if(edge==null) {
				Graphs.addEdge(grafo, rotta.getPartenza(), rotta.getDestinazione(), rotta.getAvg());
			}
			else {//quindi questo arco già esiste
				double peso= grafo.getEdgeWeight(edge);
				double newpeso=(peso+ rotta.getAvg())/2;
				grafo.setEdgeWeight(edge, newpeso);
			}
			
		}
		System.out.println(grafo);
	}
	
	public Set<Airport> getAllVertex() {
		return grafo.vertexSet();
	}
	
	public Boolean testConnessione (Integer a1, Integer a2) {
		Set<Airport> visitati= new HashSet<Airport>();
		Airport partenza= aIdMap.get(a1);
		Airport destinazione=aIdMap.get(a2);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<Airport, DefaultWeightedEdge>(grafo,partenza);
		while(it.hasNext()) {
			visitati.add(it.next());
		}
		return visitati.contains(destinazione);
		
	}
	
	public List<Airport> trovaPercorso(Integer a1, Integer a2) {
		List<Airport> percorso=new ArrayList<Airport>();
		Airport partenza= aIdMap.get(a1);
		Airport destinazione= aIdMap.get(a2);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo,partenza);
		
		visita.put(partenza, null);
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				Airport sorgente=grafo.getEdgeSource(ev.getEdge());
				Airport destinazione=grafo.getEdgeTarget(ev.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				} else if(!visita.containsKey(sorgente) && visita.containsKey(destinazione)) {
					visita.put(sorgente, destinazione);
				}
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		while (it.hasNext()) {
			it.next();
			if(!visita.containsKey(partenza) || !visita.containsKey(destinazione))
				return null;
		}
		
		Airport step=destinazione;
		while(!step.equals(partenza)) { //per tornare indietro, dalla foglia al root
			percorso.add(step);
			step=visita.get(step); //do il figlio e mi restituisce il padre
		}
		percorso.add(step);
		return percorso;

	}
}
