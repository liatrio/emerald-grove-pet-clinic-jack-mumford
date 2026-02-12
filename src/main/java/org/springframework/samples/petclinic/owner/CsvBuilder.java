/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;

/**
 * Utility class for building CSV content from owner data. Follows RFC 4180 CSV format
 * specification.
 *
 * @author Jack Mumford
 */
public class CsvBuilder {

	private static final String HEADER = "First Name,Last Name,Address,City,Telephone\n";

	private CsvBuilder() {
		// Utility class - prevent instantiation
	}

	/**
	 * Builds a CSV string from a list of owners.
	 * @param owners the list of owners to convert to CSV format
	 * @return CSV formatted string with header and owner data
	 */
	public static String buildOwnersCsv(List<Owner> owners) {
		StringBuilder csv = new StringBuilder(HEADER);
		for (Owner owner : owners) {
			csv.append(formatCsvRow(owner));
		}
		return csv.toString();
	}

	/**
	 * Formats a single owner as a CSV row.
	 * @param owner the owner to format
	 * @return CSV formatted row with newline
	 */
	private static String formatCsvRow(Owner owner) {
		return escapeCsvField(owner.getFirstName()) + "," + escapeCsvField(owner.getLastName()) + ","
				+ escapeCsvField(owner.getAddress()) + "," + escapeCsvField(owner.getCity()) + ","
				+ escapeCsvField(owner.getTelephone()) + "\n";
	}

	/**
	 * Escapes a CSV field according to RFC 4180. Fields containing commas, quotes, or
	 * newlines are wrapped in double quotes. Double quotes within the field are escaped
	 * by doubling them.
	 * @param field the field to escape
	 * @return the escaped field, or empty string if field is null
	 */
	private static String escapeCsvField(String field) {
		if (field == null) {
			return "";
		}
		if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
			return "\"" + field.replace("\"", "\"\"") + "\"";
		}
		return field;
	}

}
