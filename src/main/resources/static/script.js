// ===================== KONFIGURATION =====================
const config = {
    localApiUrl: 'http://localhost:8080/dev',
    productionApiUrl: 'https://7fm4j5apc8.execute-api.eu-west-1.amazonaws.com/dev',
};

const ELEMENT_IDS = {
    dataForm: "data-form",
    csvUploadForm: "csv-upload-form",
    csvFile: "csv-file",
    jsonResult: "json-result",
    stockChart: "stock-chart",
    costChart: "cost-chart",
    monthlyTable: "monthly-table",
};

let stockChart = null;
let costChart = null;

// ===================== EVENT-LYSSNARE =====================
document.getElementById(ELEMENT_IDS.dataForm).addEventListener("submit", handleFormSubmit);
document.getElementById(ELEMENT_IDS.csvUploadForm).addEventListener("submit", handleCsvUpload);

// ===================== API-ANROP =====================
async function callApi(formData, suffix, header) {
    const isLocal = location.hostname === "localhost";
    const baseUrl = isLocal ? config.localApiUrl : config.productionApiUrl;
    try {
        const response = await fetch(`${baseUrl}${suffix}`, {
            method: "POST",
            headers: { "Content-Type": header },
            body: formData,
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("API call failed:", error);
        return null;
    }
}

// ===================== FORM-HANTERARE =====================
async function handleFormSubmit(e) {
    e.preventDefault();
    const formDataAsString = buildFormData(e);
    const result = await callApi(formDataAsString, "/finance", "application/json");
    if (result) updateFinanceUI(result);
}

async function handleCsvUpload(e) {
    e.preventDefault();
    try {
        const csvContent = await getCsvContent(e);
        if (!isValidCsvContent(csvContent)) throw new Error("Invalid CSV format. Please check your file.");
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

// ===================== DATAHANTERING =====================
function buildFormData(event) {
    const formData = new FormData(event.target);
    return JSON.stringify({
        age: {
            birthYear: Number(formData.get("birth_year")),
            birthMonth: Number(formData.get("birth_month")),
            expectedLifespan: Number(formData.get("expected_lifespan")),
            retirementAge: Number(formData.get("retirement_age")),
        },
        income: {
            netSalary: Number(formData.get("net_salary")),
            retirementPay: Number(formData.get("retirement_pay")),
        },
        assets: {
            emergencyNow: Number(formData.get("emergency_now")),
            emergencyGoal: Number(formData.get("emergency_goal")),
            stockSavings: Number(formData.get("stock_savings")),
            stocksGain: Number(formData.get("stocks_gain")),
        },
        loans: {
            mortgage: Number(formData.get("mortgage")),
            mortgageRate: Number(formData.get("mortgage_rate")),
            csnTotal: Number(formData.get("csn_total")),
        },
        fixedCosts: {
            mustHaves: Number(formData.get("must_haves")),
            csnPayoff: Number(formData.get("csn_payoff")),
        },
        spending: {
            foodCosts: Number(formData.get("food_costs")),
            travelCosts: Number(formData.get("travel_costs")),
        },
        payChoices: {
            percentForAmortization: Number(formData.get("percent_for_amortization")),
            firePercentage: Number(formData.get("fire_percentage")),
        },
    });
}

function isValidCsvContent(content) {
    // TODO: Implementera faktisk CSV-validering
    return true;
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

// ===================== UI-UPPDATERING =====================
function updateFinanceUI(result) {
    fillShortSummary(result);
    fillFireGraph(result.monthlyData);
    buildFireTable(result.monthlyData);
}

function fillShortSummary(jsonResponse) {
    const result = { ...jsonResponse };
    delete result.monthly_data;
    document.getElementById(ELEMENT_IDS.jsonResult).innerHTML = `
        <h3>Resultat</h3>
        <ul>
          <li><strong>Ålder nödkonto fyllt:</strong> ${result.emergencyFilledAge}</li>
          <li><strong>Ålder CSN avbetalat:</strong> ${result.csnFreeAge}</li>
          <li><strong>Ålder bolån-fri:</strong> ${result.mortgageFreeAge}</li>
          <li><strong>Ålder FIRE:</strong> ${result.fireAge}</li>
          <li><strong>FIRE-summa (kr):</strong> ${Math.round(result.fireAmount).toLocaleString('sv-SE')}</li>
        </ul>
    `;
}

function fillFireGraph(monthlyData) {
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
                y: { beginAtZero: true }
            }
        }
    });
}

function updateChart(chart, monthlyData) {
    chart.data.labels = monthlyData.map(row => row.age);
    chart.data.datasets[0].data = monthlyData.map(row => row.stockSavings);
    chart.data.datasets[1].data = monthlyData.map(row => row.mortgage);
    chart.data.datasets[2].data = monthlyData.map(row => row.csnLeft);
    chart.data.datasets[3].data = monthlyData.map(row => row.fireAmount);
    chart.update();
}

function buildFireTable(monthlyData) {
    const table = document.createElement('table');
    table.className = 'savings-table';
    const formatter = new Intl.NumberFormat('sv-SE');
    const header = table.insertRow();
    ['Datum', 'Ålder', 'Investering', 'Amortering', 'Börsvärde', 'Bolån', 'FIRE behov'].forEach(text => {
        const cell = header.insertCell();
        cell.textContent = text;
    });
    for (let i = 2; i < monthlyData.length; i += 3) {
        const quarter = monthlyData.slice(i - 2, i + 1);
        const avg = arr => Math.ceil(arr.reduce((a, b) => a + b, 0) / arr.length);
        const avgInvested = avg(quarter.map(row => row.invested));
        const avgPayed = avg(quarter.map(row => row.payedOff));
        const avgSavings = Math.ceil(quarter.reduce((sum, row) => sum + row.stockSavings, 0) / quarter.length / 1000) * 1000;
        const avgMortgage = Math.max(0, Math.ceil(quarter.reduce((sum, row) => sum + row.mortgage, 0) / quarter.length / 1000) * 1000);
        const avgFire = Math.ceil(quarter.reduce((sum, row) => sum + row.fireAmount, 0) / quarter.length / 1000) * 1000;
        const row = table.insertRow();
        row.innerHTML = `
           <td>${quarter[2].date}</td>
           <td>${quarter[2].age}</td>
           <td>${formatter.format(avgInvested)} kr</td>
           <td>${formatter.format(avgPayed)} kr</td>
           <td>${formatter.format(avgSavings)} kr</td>
           <td>${formatter.format(avgMortgage)} kr</td>
           <td>${formatter.format(avgFire)} kr</td>
       `;
    }
    // Visa tabellen i DOM
    let container = document.getElementById('savings-table-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'savings-table-container';
        document.body.appendChild(container);
    }
    container.innerHTML = '';
    container.appendChild(table);
}

// ===================== CSV-CHART & TABELL =====================
function updateCsvUI(result) {
    const categories = getAllCategories(result.weeklySpecifics);
    const labels = result.weeklySpecifics.map(ws => ws.week);
    const colorList = getColorList();
    const patternList = getPatternList();
    let patternomaly = window.patternomaly;
    const avgPerCategory = {};
    const datasets = categories.map((category, idx) => {
        const data = result.weeklySpecifics.map(ws => ws.costsPerCategory && ws.costsPerCategory[category] !== undefined ? ws.costsPerCategory[category] : 0);
        const avg = data.length > 0 ? data.reduce((a, b) => a + b, 0) / data.length : 0;
        avgPerCategory[category] = avg;
        let backgroundColor = colorList[idx % colorList.length];
        if (patternomaly && patternList[idx % patternList.length]) {
            backgroundColor = patternomaly.draw(patternList[idx % patternList.length], colorList[idx % colorList.length]);
        }
        return [
            {
                label: category,
                data: data,
                backgroundColor: backgroundColor,
                type: 'bar',
                order: 1
            },
            {
                label: category + ' (snitt)',
                data: Array(data.length).fill(avg),
                borderColor: colorList[idx % colorList.length],
                borderDash: [5, 5],
                fill: false,
                type: 'line',
                pointRadius: 0,
                borderWidth: 2,
                order: 2
            }
        ];
    }).flat();
    renderCostChart(labels, datasets);
    renderAvgTable(categories, avgPerCategory);
}

function getAllCategories(weeklySpecifics) {
    const allCategories = new Set();
    weeklySpecifics.forEach(ws => {
        if (ws.costsPerCategory) {
            Object.keys(ws.costsPerCategory).forEach(cat => allCategories.add(cat));
        }
    });
    return Array.from(allCategories);
}

function getColorList() {
    return [
        'rgba(54, 162, 235, 0.7)', 'rgba(255, 99, 132, 0.7)', 'rgba(255, 206, 86, 0.7)', 'rgba(75, 192, 192, 0.7)',
        'rgba(153, 102, 255, 0.7)', 'rgba(255, 159, 64, 0.7)', 'rgba(201, 203, 207, 0.7)', 'rgba(0, 200, 83, 0.7)',
        'rgba(233, 30, 99, 0.7)', 'rgba(255, 87, 34, 0.7)', 'rgba(63, 81, 181, 0.7)', 'rgba(0, 188, 212, 0.7)',
        'rgba(205, 220, 57, 0.7)', 'rgba(121, 85, 72, 0.7)', 'rgba(158, 158, 158, 0.7)', 'rgba(0, 0, 0, 0.7)'
    ];
}

function getPatternList() {
    return [
        null, 'diagonal', 'horizontal', 'vertical', 'cross', 'dot', 'dash', 'zigzag', 'triangle', 'diamond',
        'circle', 'line', 'wave', 'grid', 'plus', 'star'
    ];
}

function renderCostChart(labels, datasets) {
    if (costChart) {
        costChart.data.labels = labels;
        costChart.data.datasets = datasets;
        costChart.update();
    } else {
        const costChartElement = document.getElementById(ELEMENT_IDS.costChart);
        costChart = new Chart(costChartElement, {
            type: 'bar',
            data: { labels, datasets },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: { boxWidth: 20, font: { size: 13 } }
                    },
                    tooltip: { mode: 'index', intersect: false }
                },
                scales: { y: { beginAtZero: true } }
            }
        });
    }
}

function renderAvgTable(categories, avgPerCategory) {
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
    ['Kategori', 'Snitt/vecka'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        th.style.padding = '6px';
        th.style.borderBottom = '1px solid #ccc';
        header.appendChild(th);
    });
    categories.forEach(category => {
        const row = avgTable.insertRow();
        const cell1 = row.insertCell();
        cell1.textContent = category;
        cell1.style.padding = '6px';
        const cell2 = row.insertCell();
        cell2.textContent = avgPerCategory[category].toLocaleString('sv-SE', { maximumFractionDigits: 0 }) + ' kr';
        cell2.style.padding = '6px';
    });

    // Lägg till autofyll-knapp efter tabellen (om den inte redan finns)
    let autofillBtn = document.getElementById('autofill-btn');
    if (!autofillBtn) {
        autofillBtn = document.createElement('button');
        autofillBtn.id = 'autofill-btn';
        autofillBtn.textContent = 'Autofyll formulär med summerade månadsutgifter';
        autofillBtn.style.margin = '20px 0 0 0';
        autofillBtn.style.padding = '10px 18px';
        autofillBtn.style.fontSize = '1em';
        autofillBtn.style.background = '#1976d2';
        autofillBtn.style.color = 'white';
        autofillBtn.style.border = 'none';
        autofillBtn.style.borderRadius = '5px';
        autofillBtn.style.cursor = 'pointer';
        avgTable.parentNode.insertBefore(autofillBtn, avgTable.nextSibling);
    }
    autofillBtn.onclick = function() {
        // Summera kategorier enligt instruktion
        const get = cat => avgPerCategory[cat] || 0;
        // food_costs = (FOOD + RESTAURANT) * 4.3
        const foodSum = (get('FOOD') + get('RESTAURANT')) * 4.3;
        // travel_costs = TRAVEL * 4.3
        const travelSum = get('TRAVEL') * 4.3;
        // must_haves = (CREDIT_CARD + ENTERTAINMENT + RENT + CHARITY + UTILITIES + HOME + HEALTH + CLOTHES + OTHER) * 4.3
        const mustHavesSum = (
            get('CREDIT_CARD') + get('ENTERTAINMENT') + get('RENT') + get('CHARITY') +
            get('UTILITIES') + get('HOME') + get('HEALTH') + get('CLOTHES') + get('OTHER')
        ) * 4.3;
        // Fyll i formulärfälten
        const setVal = (name, val) => {
            const el = document.querySelector(`[name="${name}"]`);
            if (el) el.value = Math.round(val);
        };
        setVal('food_costs', foodSum);
        setVal('travel_costs', travelSum);
        setVal('must_haves', mustHavesSum);
    };
}

// ===================== UI-FEEDBACK =====================
function showLoadingIndicator() { /* ... */ }
function hideLoadingIndicator() { /* ... */ }
function showSuccessMessage(message) { /* ... */ }
function showErrorMessage(message) { /* ... */ }