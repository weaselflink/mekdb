
const filters = {
	query: "",
	tech: "All",
	level: "All",
};

const applyFilters = (row) => {
	const model = row.querySelector("td.model").textContent.toLowerCase();
	const chassis = row.querySelector("td.chassis").textContent.toLowerCase();
	const tech = row.querySelector("td.tech").textContent;
	const level = row.querySelector("td.level").textContent;
	const weapons = row.querySelector("td.weapons").textContent.toLowerCase();

	if (filters.query !== "") {
		if (!model.includes(filters.query) && !chassis.includes(filters.query) && !weapons.includes(filters.query)) {
			return false;
		}
	}

	if (filters.tech !== "All") {
		if (tech !== filters.tech) {
			return false;
		}
	}

	if (filters.level !== "All") {
		if (level !== filters.level) {
			return false;
		}
	}

	return true;
}

const updateTable = () => {
	document.querySelectorAll("table tbody tr")
		.forEach(row => {
			row.classList.remove("hidden");
			if (!applyFilters(row)) {
				row.classList.add("hidden");
			}
		})
};

const textSearch = document.querySelector(".filters .search");
textSearch.value = "";

textSearch.oninput = () => {
	filters.query = textSearch.value.toLowerCase();
	updateTable();
};

const techSelect = document.querySelector(".filters .tech");
techSelect.value = "All";

techSelect.oninput = () => {
	filters.tech = techSelect.value;
	updateTable();
};

const levelSelect = document.querySelector(".filters .level");
levelSelect.value = "All";

levelSelect.oninput = () => {
	filters.level = levelSelect.value;
	updateTable();
};
