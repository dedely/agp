package business.engine;

import java.util.Comparator;
import java.util.LinkedList;

import business.model.Excursion;
import business.model.location.Site;
import business.model.transport.Transport;

public class ExcursionComparator implements Comparator<Excursion> {

	private static final double COMFORT_DISTANCE_THRESHOLD = 0;

	private int comfortPreference = 2;

	@Override
	public int compare(Excursion e1, Excursion e2) {
		int priceE1 = calculateExcursionPrice(e1);
		int priceE2 = calculateExcursionPrice(e2);
		int priceComparison = Integer.signum(priceE2 - priceE1); // lower is better

		int comfortE1 = calculateExcursionComfort(e1);
		int comfortE2 = calculateExcursionComfort(e2);

		// Calculate the distance in comfort between the actual comfort of the excursion
		// and the user preference
		int distE1 = Math.abs(comfortE1 - comfortPreference);
		int distE2 = Math.abs(comfortE2 - comfortPreference);
		int comfortComparison = Integer.signum(distE2 - distE1); // lower is better

		return -(priceComparison + comfortComparison); //decreasing order
	}

	public void setComfortPreference(int comfortPreference) {
		this.comfortPreference = comfortPreference;
	}

	private int calculateExcursionPrice(Excursion excursion) {
		int visitPrices = 0;
		int transportPrices = 0;
		LinkedList<Site> sites = excursion.getSites();
		for (Site site : sites) {
			visitPrices += site.getPrice();
		}

		LinkedList<LinkedList<Transport>> transports = excursion.getTransports();
		for (LinkedList<Transport> transportsBetweenLocations : transports) {
			for (Transport transport : transportsBetweenLocations) {
				transportPrices += transport.getPrice();
			}
		}
		excursion.setPrice(visitPrices + transportPrices);
		return excursion.getPrice();
	}

	private double calculateExcursionTotalDistance(Excursion excursion) {
		double totalDistance = 0;
		LinkedList<Site> sites = excursion.getSites();
		for (int i = 1; i < sites.size(); i++) {
			totalDistance += EngineUtility.calculateDistance(sites.get(i - 1), sites.get(i));

			if (i == 1 || i == sites.size() - 1) {
				totalDistance += EngineUtility.calculateDistance(excursion.getHotel(), sites.get(i));
			}
		}

		return totalDistance;
	}

	private int calculateExcursionComfort(Excursion excursion) {
		int scoreByType = 0;
		int scoreByDistance = 2;
		LinkedList<Site> sites = excursion.getSites();
		for (Site site : sites) {
			scoreByType += site.getTransport().getConfort();
			scoreByType += site.getConfort();
		}

		double totalDistance = calculateExcursionTotalDistance(excursion);

		if (totalDistance > COMFORT_DISTANCE_THRESHOLD) {
			scoreByDistance = 1;
		}

		int div = sites.size() - 2;
		scoreByType = (div != 0) ? scoreByType / (2 * div) : 0;
		return (scoreByType + scoreByDistance) / 2;
	}

}
