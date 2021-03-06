package beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import business.engine.SearchEntry;

@ManagedBean
@SessionScoped

public class EntryBean implements Serializable {

	private static final long serialVersionUID = -7658310028694454686L;

	private SearchEntry entry = new SearchEntry();

	private String query;

	public EntryBean() {
	}

	@PostConstruct
	public void Init() {
		setComfortPreference(2);
	}

	public String startEngine() {
		if ((getBudgetMax() == 0)
				&& ((getQuery().trim().isEmpty()) || ((getQuery().isEmpty()) || (getQuery().equals(null))))) {
			return "insufficient-data";
		} else if (getBudgetMin() > getBudgetMax()) {
			return "invalid-budget";
		} else {
			buildKeywordList(getQuery());
			return "result";
		}
	}

	public List<String> buildKeywordList(String query) {
		List<String> results = getSearchKeywords();
		entry.setSearchKeywords(new ArrayList<String>());
		if (!query.equals(null)) {
			String[] keywords = query.split(" ");

			for (String k : keywords) {
				if (!k.isEmpty()) {
					results.add(k);
				}
			}
		}
		return results;
	}

	public SearchEntry getEntry() {
		return entry;
	}

	public void setEntry(SearchEntry entry) {
		this.entry = entry;
	}

	public int getBudgetMin() {
		return (entry.getBudgetMin() != null) ? entry.getBudgetMin().intValue() : 0;
	}

	public void setBudgetMin(int budgetMin) {
		entry.setBudgetMin(budgetMin);
	}

	public int getBudgetMax() {
		return (entry.getBudgetMax() != null) ? entry.getBudgetMax().intValue() : 0;
	}

	public void setBudgetMax(int budgetMax) {
		entry.setBudgetMax(budgetMax);
	}

	public int getDaysOfStay() {
		return (entry.getDaysOfStay() != null) ? entry.getDaysOfStay().intValue() : 0;
	}

	public void setDaysOfStay(int daysOfStay) {
		entry.setDaysOfStay(daysOfStay);
	}

	public int getComfortPreference() {
		return (entry.getComfortPreference() != null) ? entry.getComfortPreference().intValue() : 0;
	}

	public void setComfortPreference(int comfortPreference) {
		entry.setComfortPreference(comfortPreference);
	}

	public List<String> getSearchKeywords() {
		return entry.getSearchKeywords();
	}

	public void setSearchKeywords(List<String> searchKeywords) {
		entry.setSearchKeywords(searchKeywords);
	}

	public void addSearchKeyword(String keyword) {
		if (!entry.getSearchKeywords().contains(keyword)) {
			entry.addSearchKeyword(keyword);
		}
	}

	public void removeSearchKeyword(String keyword) {
		entry.removeSearchKeyword(keyword);
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}