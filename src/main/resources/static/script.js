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
  savingsTableContainer: "savings-table-container",
  graphContainer: "graph-container",
  monthlyTable: "monthly-table",
}

let stockChart = null;

document.getElementById(ELEMENT_IDS.dataForm).addEventListener("submit", handleFormSubmit);
//document.getElementById(ELEMENT_IDS.csvUploadForm).addEventListener("submit", handleFormSubmit);

async function handleFormSubmit(e) {
    e.preventDefault();
    const formDataAsString = buildFormData(e);
    const result = await callApi(formDataAsString, "/finance");
    if (result) {
        updateUI(result);
    }
}

async function callApi(formData, suffix) {
    const isLocal = location.hostname === "localhost";
    const baseUrl = isLocal? config.localApiUrl : config.productionApiUrl;

    try {
        const response = await fetch(`${baseUrl}${suffix}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
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

function updateUI(result) {
    fillShortSummary(result)
    fillGraph(result.monthlyData)
    buildMonthlyTable(result.monthlyData)
}

/*document.getElementById("csv-upload-form").addEventListener("submit", async function(event) {
    event.preventDefault();
    const fileInput = document.getElementById("csv-file");
    const file = fileInput.files[0];

    if (file && file.name.endsWith(".csv")) {
        const reader = new FileReader();

        reader.onload = function(e) {
            const content = e.target.result;
            processCsv(content)
        };

        reader.readAsText(file);
    } else {
        alert("Vänligen välj en csv-fil.")
    }
});

function processCsv(content) {
    const lines = content.split("\n");
    console.log(lines)
}*/

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

    document.getElementById("json-result").innerHTML = `
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

function fillGraph(monthlyData) {
    console.log("filling graph");
    if (stockChart) {
        updateChart(stockChart, monthlyData);
    } else {
        stockChart = createChart(monthlyData);
    }
}

function createChart(monthlyData) {
    const age = monthlyData.map(row => row.age);
    const stockSavings = monthlyData.map(row => row.stockSavings);
    const mortgage = monthlyData.map(row => row.mortgage);
    const csnLeft = monthlyData.map(row => row.csnLeft);
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

function buildMonthlyTable(monthlyData) {
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

        const invested = quarter.reduce((sum, row) => sum  + row.invested, 0)
        const payed = quarter.reduce((sum, row) => sum  + row.payedOff, 0)
        const savings = quarter.reduce((sum, row) => sum  + row.stockSavings, 0)
        const mortgage = quarter.reduce((sum, row) => sum  + row.mortgage, 0)
        const fire = quarter.reduce((sum, row) => sum  + row.fireAmount, 0)

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