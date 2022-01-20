package business.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import business.model.Excursion;
import business.model.HotelReservation;
import business.model.Offer;
import business.model.location.Hotel;
import business.model.location.Site;
import business.model.transport.Boat;
import business.model.transport.Transport;
import business.spring.SpringIoC;
import dao.LocationPersistence;
import persistence.EDBLocationPersistence;

public class SimpleOfferBuilder implements OfferBuilder {
	private static final int MIN_VISIT_PER_EXCURSION = 1;
	private static final int MAX_VISIT_PER_EXCURSION = 2;
	private static final int AVG_COMFORT = 2;

	private static final int DEFAULT_BUDGET = 1000;

	private SearchEntry searchEntry;
	private LocationPersistence locationPersistence;
	private String tableName = "sites";
	private String key = "name";
	private String userDirPath = System.getProperty("user.dir") + "/tmp/sites";

	private List<Offer> offers = new LinkedList<Offer>();
	private List<Hotel> hotels = new ArrayList<Hotel>();
	private List<Site> sites = new ArrayList<Site>();

	private List<List<Integer>> siteSubsets;
	private List<Site> visitedSites;
	private List<Excursion> excursions;
	private double[][] distanceMatrix;

	private double excursionFrequency;

	public SimpleOfferBuilder() {
		locationPersistence = new EDBLocationPersistence(tableName, key, userDirPath);
	}

	public SimpleOfferBuilder(SearchEntry searchEntry) {
		this.searchEntry = searchEntry;
		locationPersistence = new EDBLocationPersistence(tableName, key, userDirPath);
	}

	@Override
	public void setSearchEntry(SearchEntry searchEntry) {
		this.searchEntry = searchEntry;
	}

	@Override
	public List<Offer> getOffers() {
		return offers;
	}

	@Override
	public void build() {

		getCandidateHotels();
		getCandidateSites();

		if (!hotels.isEmpty() && !!sites.isEmpty()) {

			findIndexSubsets(sites.size(), MIN_VISIT_PER_EXCURSION, MAX_VISIT_PER_EXCURSION);

			calculateDistances();

			buildExcursions();

			rankExcursions();

			buildOffers();
		}

	}

	private void buildOffers() {
		excursionFrequency = isSetComfortPreference() ? searchEntry.getComfortPreference() : AVG_COMFORT;

		Offer offer;
		int nbOffer = 3;
		int i = 0;
		int time;
		int day;
		int currentHotelStayDuration;
		int budget = isSetPriceRange() ? searchEntry.getBudgetMax() : DEFAULT_BUDGET;
		Hotel currentHotel;
		HotelReservation reservation;
		int currentOfferPrice = 0;
		Excursion excursion;
		boolean alreadyVisited;
		boolean newExcursionFound;

		excursionFrequency = isSetComfortPreference() ? 1 / searchEntry.getComfortPreference() : 1 / AVG_COMFORT;
		while (i < nbOffer && !excursions.isEmpty()) {
			offer = new Offer();
			time = 0;
			day = 0;
			currentHotelStayDuration = 0;
			alreadyVisited = false;
			excursion = excursions.get(0);
			visitedSites.clear();
			currentHotel = excursions.get(0).getHotel();
			newExcursionFound = true;

			while (currentOfferPrice <= budget && newExcursionFound) {
				if (time % 2 == 0) {
					if (currentOfferPrice + currentHotel.getPricePerNight() > budget) {
						break;
					}
					currentHotelStayDuration++;
					day++;
				}

				if (Math.random() <= excursionFrequency) {
					// Get next excursion
					Iterator<Excursion> iterator = excursions.iterator();
					boolean found = false;
					while (iterator.hasNext() && !found) {
						excursion = iterator.next();

						for (Site site : excursion.getSites()) {
							if (visitedSites.contains(site)) {
								/*
								 * Hotel nextHotel = excursion.getHotel(); if
								 * (!nextHotel.getName().equals(currentHotel.getName())) { currentHotel =
								 * excursion.getHotel();
								 * 
								 * int transportPrice = nextHotel.getTransport().getPrice(); if
								 * (currentOfferPrice + transportPrice > budget) { break; } }
								 */

								if (currentOfferPrice + excursion.getPrice() <= budget) {
									alreadyVisited = true;
									break;
								}

							}
						}

						if (!alreadyVisited) {
							found = true;

							Hotel nextHotel = excursion.getHotel();
							if (!nextHotel.getName().equals(currentHotel.getName())) {
								reservation = new HotelReservation(currentHotel, currentHotelStayDuration);
								offer.addHotelReservation(reservation);
								currentHotel = nextHotel;
								currentHotelStayDuration = 0;
							}
							
							
							excursion.setDay(day);
							offer.addExcursion(excursion);
							for (Site site : excursion.getSites()) {
								visitedSites.add(site);
							}
							currentOfferPrice += excursion.getPrice();
						}
					}

					if (found) {
						excursions.remove(excursion);
					} else {
						newExcursionFound = false;
					}

				}
				time++;
			}
			reservation = new HotelReservation(currentHotel, currentHotelStayDuration);
			offer.addHotelReservation(reservation);
			offer.setTotalPrice(currentOfferPrice);
			i++;
		}

	}

	private void rankExcursions() {
		ExcursionComparator comparator = new ExcursionComparator();
		if (isSetComfortPreference()) {
			comparator.setComfortPreference(searchEntry.getComfortPreference());
		}
		excursions.sort(comparator);
	}

	private void findIndexSubsets(int n, int minSize, int maxSize) {
		for (int k = minSize; k <= maxSize; k++) {
			recSubsetBuilder(n, k, 0, siteSubsets, new ArrayList<>());
		}
	}

	private void recSubsetBuilder(int n, int k, int startIndex, List<List<Integer>> subsets, List<Integer> subset) {
		if (k == subset.size()) {
			subsets.add(new ArrayList<Integer>(subset));
			return;
		}
		for (int i = startIndex; i <= n; i++) {
			subset.add(i);
			recSubsetBuilder(n, k, i + 1, subsets, subset);
			subset.remove(subset.size() - 1);
		}
	}

	private void buildExcursions() {
		excursions = new ArrayList<Excursion>();
		DistanceComparator comparator = new DistanceComparator(distanceMatrix);
		Boat boat = (Boat) SpringIoC.getBean("boat");
		for (Hotel hotel : hotels) {
			comparator.setIdHotel(hotels.indexOf(hotel));

			for (List<Integer> subset : siteSubsets) {

				Excursion excursion = new Excursion();
				LinkedList<LinkedList<Transport>> transports = new LinkedList<LinkedList<Transport>>();
				excursion.setHotel(hotel);
				subset.sort(comparator);
				String previousIsland = hotel.getIsland();

				for (int i = 0; i < subset.size(); i++) {

					Site site = sites.get(i);
					LinkedList<Transport> transportsToSite = new LinkedList<Transport>();

					excursion.addSite(site);
					transportsToSite.add(site.getTransport());

					if (!site.getIsland().equals(previousIsland)) {
						transportsToSite.add(boat);
						transportsToSite.add(site.getTransport());
					}

					previousIsland = site.getIsland();
					transports.add(transportsToSite);

					if (i == subset.size() - 1) {
						LinkedList<Transport> transportsToHotel = new LinkedList<Transport>();
						transportsToHotel.add(hotel.getTransport());
						if (!previousIsland.equals(hotel.getIsland())) {
							transportsToHotel.add(boat);
							transportsToHotel.add(hotel.getTransport());
						}
						transports.add(transportsToHotel);
					}
				}

				excursions.add(excursion);
			}
		}
	}

	private void getCandidateHotels() {
		if (isSetPriceRange()) {
			hotels = locationPersistence.getHotelByPrice(searchEntry.getBudgetMin(), searchEntry.getBudgetMax());
		} else {
			hotels = locationPersistence.getAllHotels();
		}
	}

	private void getCandidateSites() {
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		if (isSetComfortPreference()) {
			queryParameters.put("confort", searchEntry.getComfortPreference());
		}
		if (isSetPriceRange()) {
			queryParameters.put("minPrice", searchEntry.getBudgetMin());
			queryParameters.put("maxPrice", searchEntry.getBudgetMax());
		}
		if (isSetKeywords()) {
			queryParameters.put("keywords", searchEntry.getSearchKeywords());
		}
		sites = locationPersistence.getSiteByParameters(queryParameters);
	}

	private boolean isSetKeywords() {
		return searchEntry.getSearchKeywords().size() > 0;
	}

	private boolean isSetPriceRange() {
		return searchEntry.getBudgetMin() != null && searchEntry.getBudgetMax() != null;
	}

	private boolean isSetComfortPreference() {
		return searchEntry.getComfortPreference() != null;
	}

	private void calculateDistances() {
		distanceMatrix = new double[hotels.size()][sites.size()];
		for (int i = 0; i < hotels.size(); i++) {
			for (int j = 0; i < sites.size(); j++) {
				distanceMatrix[i][j] = EngineUtility.calculateDistance(hotels.get(i), sites.get(j));
			}
		}
	}

	public SearchEntry getSearchEntry() {
		return searchEntry;
	}

}
