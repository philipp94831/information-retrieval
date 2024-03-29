package de.hpi.ir.yahoogle.parsing;

class Citation {

	private String country;
	private int date;
	private Integer docNumber;

	public int getDocNumber() {
		return docNumber;
	}

	public boolean isValid() {
		return docNumber != null && date > 20110000 && country.equals("US");
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public void setDocNumber(int docNumber) {
		this.docNumber = docNumber;
	}
}
