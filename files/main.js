
const textSearch = document.querySelector(".filters .search");
textSearch.value = "";

textSearch.oninput = () => {
	const query = textSearch.value.toLowerCase();
	if (!query || query === "") {
		document.querySelectorAll("table tbody tr")
			.forEach(row => row.classList.remove("hidden"));
	} else {
		document.querySelectorAll("table tbody tr")
			.forEach(row => {
				const model = row.querySelector("td.model").textContent.toLowerCase();
				const chassis = row.querySelector("td.chassis").textContent.toLowerCase();
				const weapons = row.querySelector("td.weapons").textContent.toLowerCase();
				row.classList.add("hidden");
				if (model.includes(query) || chassis.includes(query) || weapons.includes(query)) {
					row.classList.remove("hidden");
				}
			})
	}
};
