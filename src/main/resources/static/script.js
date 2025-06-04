const config = {
    localApiUrl: 'http://localhost:8080/dev',
    productionApiUrl: 'https://7fm4j5apc8.execute-api.eu-west-1.amazonaws.com/dev',
}

const ELEMENT_IDS = {
    dataForm: "data-form",
    csvUploadForm: "csv-upload-form",
    csvFile: "csv-file",
    jsonResult: "json-result",
    stockChart: "stock-chart",
    costChart: "cost-chart",
    monthlyTable: "monthly-table",
}

let stockChart = null;
let costChart = null;

document.getElementById(ELEMENT_IDS.dataForm).addEventListener("submit", handleFormSubmit);
document.getElementById(ELEMENT_IDS.csvUploadForm).addEventListener("submit", handleCsvUpload);

async function callApi(formData, suffix, header) {
    const isLocal = location.hostname === "localhost";
    const baseUrl = isLocal ? config.localApiUrl : config.productionApiUrl;

    try {
        const response = await fetch(`${baseUrl}${suffix}`, {
            method: "POST",
            headers: {
                "Content-Type": header,
            },
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error("API call failed:", error)
        return null;
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const formDataAsString = buildFormData(e);
    const result = await callApi(formDataAsString, "/finance", "application/json");
    if (result) {
        updateFinanceUI(result);
    }
}

async function handleCsvUpload(e) {
    e.preventDefault();
    try {
        const csvContent = await getCsvContent(e);
        if (!isValidCsvContent(csvContent)) {
            throw new Error("Invalid CSV format. Please check your file.");
        }

        showLoadingIndicator();
        const result = await callApi(csvContent, "/csv", "text/csv");
        hideLoadingIndicator();

        if (result) {
            console.log(result);
            updateCsvUI(result);
            showSuccessMessage("CSV data processed successfully!");
        }
    } catch (error) {
        console.error("Error uploading CSV:", error);
        showErrorMessage(error.message);
    }
}

function isValidCsvContent(content) {
    // Implement basic CSV validation logic here
    // For example, check if it has the expected number of columns
    return true;
    //    const lines = content.split("\n");
    //    return lines.length > 1 && lines[0].split(",").length === expectedColumnCount;
}

function getCsvContent(event) {
    return new Promise((resolve, reject) => {
        const fileInput = document.getElementById(ELEMENT_IDS.csvFile);
        const file = fileInput.files[0];
        if (file && file.name.endsWith(".csv")) {
            const reader = new FileReader();
            reader.onload = (e) => resolve(e.target.result);
            reader.onerror = () => reject(new Error("Failed to read the file."));
            reader.readAsText(file);
        } else {
            reject(new Error("Please select a valid CSV file."));
        }
    });
}

function updateFinanceUI(result) {
    fillShortSummary(result)
    fillFireGraph(result.monthlyData)
    buildFireTable(result.monthlyData)
}

function buildFormData(event) {
    const formData = new FormData(event.target)
    const formDataAsJson = {
        "age": {
            "birthYear": Number(formData.get("birth_year")),
            "birthMonth": Number(formData.get("birth_month")),
            "expectedLifespan": Number(formData.get("expected_lifespan")),
            "retirementAge": Number(formData.get("retirement_age"))
        },
        "income": {
            "netSalary": Number(formData.get("net_salary")),
            "retirementPay": Number(formData.get("retirement_pay"))
        },
        "assets": {
            "emergencyNow": Number(formData.get("emergency_now")),
            "emergencyGoal": Number(formData.get("emergency_goal")),
            "stockSavings": Number(formData.get("stock_savings")),
            "stocksGain": Number(formData.get("stocks_gain"))
        },
        "loans": {
            "mortgage": Number(formData.get("mortgage")),
            "mortgageRate": Number(formData.get("mortgage_rate")),
            "csnTotal": Number(formData.get("csn_total"))
        },
        "fixedCosts": {
            "mustHaves": Number(formData.get("must_haves")),
            "csnPayoff": Number(formData.get("csn_payoff"))
        },
        "spending": {
            "foodCosts": Number(formData.get("food_costs")),
            "travelCosts": Number(formData.get("travel_costs"))
        },
        "payChoices": {
            "percentForAmortization": Number(formData.get("percent_for_amortization")),
            "firePercentage": Number(formData.get("fire_percentage"))
        }
    };

    const formDataAsString = JSON.stringify(formDataAsJson)
    return formDataAsString;
}

function formatDate(date) {
    const [year, month, day] = date.split("-")
    return `${year}-${month}`
}

function fillShortSummary(jsonResponse) {
    console.log("filling short summary")
    const result = { ...jsonResponse }
    delete result.monthly_data

    document.getElementById(ELEMENT_IDS.jsonResult).innerHTML = `
        <h3>Resultat</h3>
        <u1>
          <li><strong>Ålder nödkonto fyllt:</strong>${result.emergencyFilledAge}</li>
          <li><strong>Ålder CSN avbetalat:</strong>${result.csnFreeAge}</li>
          <li><strong>Ålder bolån-fri:</strong>${result.mortgageFreeAge}</li>
          <li><strong>Ålder FIRE:</strong>${result.fireAge}</li>
          <li><strong>FIRE-summa (kr):</strong>${Math.round(result.fireAmount).toLocaleString('sv-SE')}</li>
        </u1>
       `
}

function fillFireGraph(monthlyData) {
    console.log("filling graph");
    if (stockChart) {
        updateChart(stockChart, monthlyData);
    } else {
        stockChart = createFireChart(monthlyData);
    }
}

function createFireChart(monthlyData) {
    const age = monthlyData.map(row => row.age);
    const stockSavings = monthlyData.map(row => row.stockSavings);
    const mortgage = monthlyData.map(row => row.mortgage);
    const csnLeft = monthlyData.map(row => row.csnDebt);
    const fireAmount = monthlyData.map(row => row.fireAmount);
    const stockChartElement = document.getElementById(ELEMENT_IDS.stockChart);

    return new Chart(stockChartElement, {
        type: 'line',
        data: {
            labels: age,
            datasets: [
                { label: 'Aktier', data: stockSavings, borderColor: 'green' },
                { label: 'Bolån', data: mortgage, borderColor: 'yellow' },
                { label: 'CSN Lån', data: csnLeft, borderColor: 'red' },
                { label: 'FIRE-mål', data: fireAmount, borderColor: 'orange', borderDash: [5, 5] }
            ]
        },
        options: {
            scales: {
                x: {
                    ticks: {
                        callback: function (value, index) {
                            return index % 2 === 0 ? this.getLabelForValue(value) : '';
                        }
                    }
                },
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function updateChart(chart, monthlyData) {
    const age = monthlyData.map(row => row.age);
    const stockSavings = monthlyData.map(row => row.stockSavings);
    const mortgage = monthlyData.map(row => row.mortgage);
    const csnLeft = monthlyData.map(row => row.csnLeft);
    const fireAmount = monthlyData.map(row => row.fireAmount);

    chart.data.labels = age;
    chart.data.datasets[0].data = stockSavings;
    chart.data.datasets[1].data = mortgage;
    chart.data.datasets[2].data = csnLeft;
    chart.data.datasets[3].data = fireAmount;
    chart.update();
}

function buildFireTable(monthlyData) {
    console.log("filling savings table");
    const table = document.createElement('table');
    table.className = 'savings-table';
    const formatter = new Intl.NumberFormat('sv-SE');

    //   Skapa rubrikrad
    const header = table.insertRow();
    header.insertCell().textContent = 'Datum'
    header.insertCell().textContent = 'Ålder'
    header.insertCell().textContent = 'Investering'
    header.insertCell().textContent = 'Amortering'
    header.insertCell().textContent = 'Börsvärde'
    header.insertCell().textContent = 'Bolån'
    header.insertCell().textContent = 'FIRE behov'

    for (let i = 2; i < monthlyData.length; i += 3) {
        //  Ta ut senaste 3 månaderna
        const quarter = monthlyData.slice(i - 2, i + 1);

        const invested = quarter.reduce((sum, row) => sum + row.invested, 0)
        const payed = quarter.reduce((sum, row) => sum + row.payedOff, 0)
        const savings = quarter.reduce((sum, row) => sum + row.stockSavings, 0)
        const mortgage = quarter.reduce((sum, row) => sum + row.mortgage, 0)
        const fire = quarter.reduce((sum, row) => sum + row.fireAmount, 0)

        const avgInvested = Math.ceil(invested / quarter.length / 100) * 100;
        const avgPayed = Math.ceil(payed / quarter.length / 100) * 100;
        const avgSavings = Math.ceil(savings / quarter.length / 1000) * 1000;
        const avgMortgage = Math.max(0, Math.ceil(mortgage / quarter.length / 1000) * 1000);
        const avgFire = Math.ceil(fire / quarter.length / 1000) * 1000;

        const date = monthlyData[i].date;
        const age = monthlyData[i].age;

        const row = table.insertRow();
        row.innerHTML = `
           <td>${date}</td>
           <td>${age}</td>
           <td>${formatter.format(avgInvested)} kr</td>
           <td>${formatter.format(avgPayed)} kr</td>
           <td>${formatter.format(avgSavings)} kr</td>
           <td>${formatter.format(avgMortgage)} kr</td>
           <td>${formatter.format(avgFire)} kr</td>
       `;
    }
}

function updateCsvUI(result) {
    // Samla alla kategorier som förekommer i datan
    const allCategories = new Set();
    result.weeklySpecifics.forEach(ws => {
        if (ws.costsPerCategory) {
            Object.keys(ws.costsPerCategory).forEach(cat => allCategories.add(cat));
        }
    });
    const categories = Array.from(allCategories);
    const labels = result.weeklySpecifics.map(ws => ws.week);

    // Skapa datasets dynamiskt för varje kategori
    const colorList = [
        'blue', 'green', 'orange', 'gray', 'red', 'purple', 'brown', 'pink', 'teal', 'yellow', 'black'
    ];
    // Spara snitt per kategori för tabellen
    const avgPerCategory = {};
    const datasets = categories.map((category, idx) => {
        const data = result.weeklySpecifics.map(ws => ws.costsPerCategory && ws.costsPerCategory[category] !== undefined ? ws.costsPerCategory[category] : 0);
        // Beräkna medelvärde för kategorin
        const sum = data.reduce((a, b) => a + b, 0);
        const avg = data.length > 0 ? sum / data.length : 0;
        avgPerCategory[category] = avg;
        // Lägg till en dataset för staplarna
        const barDataset = {
            label: category,
            data: data,
            backgroundColor: colorList[idx % colorList.length],
            type: 'bar',
            order: 1
        };
        // Lägg till en dataset för medellinjen
        const avgDataset = {
            label: category + ' (snitt)',
            data: Array(data.length).fill(avg),
            borderColor: colorList[idx % colorList.length],
            borderDash: [5, 5],
            fill: false,
            type: 'line',
            pointRadius: 0,
            borderWidth: 2,
            order: 2
        };
        return [barDataset, avgDataset];
    }).flat();

    if (costChart) {
        costChart.data.labels = labels;
        costChart.data.datasets = datasets;
        costChart.update();
    } else {
        const costChartElement = document.getElementById(ELEMENT_IDS.costChart);
        costChart = new Chart(costChartElement, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: datasets
            },
            options: {
                responsive: true,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    }

    // Skapa och visa tabell med snitt per kategori
    let avgTable = document.getElementById('avg-table');
    if (!avgTable) {
        avgTable = document.createElement('table');
        avgTable.id = 'avg-table';
        avgTable.style.marginTop = '20px';
        avgTable.style.borderCollapse = 'collapse';
        avgTable.style.background = '#f9f9f9';
        avgTable.style.fontFamily = 'sans-serif';
        avgTable.style.fontSize = '1em';
        avgTable.style.boxShadow = '0 2px 8px #eee';
        const container = document.getElementById('csv-results');
        container.appendChild(avgTable);
    }
    avgTable.innerHTML = '';
    const header = avgTable.insertRow();
    const h1 = document.createElement('th');
    h1.textContent = 'Kategori';
    h1.style.padding = '6px';
    h1.style.borderBottom = '1px solid #ccc';
    header.appendChild(h1);
    const h2 = document.createElement('th');
    h2.textContent = 'Snitt/vecka';
    h2.style.padding = '6px';
    h2.style.borderBottom = '1px solid #ccc';
    header.appendChild(h2);
    categories.forEach(category => {
        const row = avgTable.insertRow();
        const cell1 = row.insertCell();
        cell1.textContent = category;
        cell1.style.padding = '6px';
        const cell2 = row.insertCell();
        cell2.textContent = avgPerCategory[category].toLocaleString('sv-SE', {maximumFractionDigits: 0}) + ' kr';
        cell2.style.padding = '6px';
    });
}

function showLoadingIndicator() {
    // Implement this to show a loading spinner or message
}

function hideLoadingIndicator() {
    // Implement this to hide the loading spinner or message
}

function showSuccessMessage(message) {
    // Implement this to show a success message to the user
}

function showErrorMessage(message) {
    // Implement this to show an error message to the user
}