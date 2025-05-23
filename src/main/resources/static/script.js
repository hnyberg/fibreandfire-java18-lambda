let stockChart = null;

document.getElementById("dataForm").addEventListener("submit", async (e) => {
      e.preventDefault();

      const formDataAsString = buildFormData(e);

      const isLocal = location.hostname === 'localhost' || location.protocol === 'file:';
      const baseUrl = isLocal
        ? 'http://localhost:8080/dev/finance'
        : 'https://7fm4j5apc8.execute-api.eu-west-1.amazonaws.com/dev/finance';

      const response = await fetch(baseUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: formDataAsString
      });

      if (response.ok) {
        const jsonResponse = await response.json();
        fillShortSummary(jsonResponse)

        const monthlyData = jsonResponse.monthlyData
        fillGraph(monthlyData)
        buildMonthlyTable(monthlyData)

      } else {
        console.error("Något gick fel:", response.status)
      }

    });

function buildFormData(event) {
    const formData = new FormData(event.target)
      const formDataAsJson = {
        "birth": {
          "year": Number(formData.get("birth_year")),
          "month": Number(formData.get("birth_month"))
        },
        "income": {
          "salary": Number(formData.get("salary"))
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
          <li><strong>Nödkonto fyllt:</strong>${formatDate(result.emergencyFilledDate)}</li>
          <li><strong>CSN avbetalt:</strong>${formatDate(result.csnFreeDate)}</li>
          <li><strong>Bostadslån betalat:</strong>${formatDate(result.mortgageFreeDate)}</li>
          <li><strong>FIRE-tid:</strong>${formatDate(result.fireDate)}</li>
          <li><strong>FIRE-ålder:</strong>${result.fireAge}</li>
          <li><strong>FIRE-summa (kr):</strong>${Math.round(result.fireAmount).toLocaleString('sv-SE')}</li>
        </u1>
       `
}

function fillGraph(monthlyData) {
    console.log("filling graph")
    const dates = monthlyData.map(row => row.date)
    const age = monthlyData.map(row => row.age)
    const saved = monthlyData.map(row => row.saved)
    const stockSavings = monthlyData.map(row => row.stockSavings)
    const payedOff = monthlyData.map(row => row.payedOff)
    const mortgageLeft = monthlyData.map(row => row.mortgageLeft)
    const csnLeft = monthlyData.map(row => row.csnLeft)
    const fireAmount = monthlyData.map(row => row.fireAmount)
    const stockChartElement = document.getElementById('stockChart')

    if (stockChart) {
                stockChart.data.labels = dates;
                stockChart.data.datasets[0].data = stockSavings
                stockChart.data.datasets[1].data = mortgageLeft
                stockChart.data.datasets[2].data = csnLeft
                stockChart.data.datasets[3].data = fireAmount
                stockChart.update()
            } else {
                stockChart = new Chart(stockChartElement, {
                    type: 'line',
                    data: {
                        labels: dates,
                        datasets: [
                            { label: 'Aktier', data: stockSavings, borderColor: 'green' },
                            { label: 'Bolån', data: mortgageLeft, borderColor: 'yellow' },
                            { label: 'CSN Lån', data: csnLeft, borderColor: 'red' },
                            { label: 'FIRE-mål', data: fireAmount, borderColor: 'orange', borderDash: [5, 5] }
                        ]
                    },
                    options: {
                        scales: {
                            x: {
                                ticks: {
                                    callback: function (value, index) {
                                        if (index % 2 == 0) return this.getLabelForValue(value)
                                            return ``
                                        }
                                    }
                                },
                            y: {
                                beginAtZero: true
                            }
                        }
                    }
                })
            }
}

function buildMonthlyTable(monthlyData) {
   console.log("filling savings table")
   const table = document.createElement('table');
   table.className = 'savings-table';

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

        const age = quarter[0].age;
        const saved = quarter.reduce((sum, row) => sum  + row.saved, 0);
        const payed = quarter.reduce((sum, row) => sum  + row.payedOff, 0);
        const savings = quarter.reduce((sum, row) => sum  + row.stockSavings, 0);
        const mortgage = quarter.reduce((sum, row) => sum  + row.mortgageLeft, 0);
        const fire = quarter.reduce((sum, row) => sum  + row.fireAmount, 0);

        const avgSaved = Math.ceil(saved / quarter.length / 100) * 100;
        const avgPayed = Math.ceil(payed / quarter.length / 100) * 100;
        const avgSavings = Math.ceil(savings / quarter.length / 1000) * 1000;
        const avgMortgage = Math.max(0, Math.ceil(mortgage / quarter.length / 1000) * 1000);
        const avgFire = Math.ceil(fire / quarter.length / 1000) * 1000;

        const dateLabel = monthlyData[i].date;

        const row = table.insertRow();
        row.insertCell().textContent = dateLabel;
        row.insertCell().textContent = age + 'år';
        row.insertCell().textContent = avgSaved + ' kr';
        row.insertCell().textContent = avgPayed + ' kr';
        row.insertCell().textContent = avgSavings + ' kr';
        row.insertCell().textContent = avgMortgage + ' kr';
        row.insertCell().textContent = avgFire + ' kr';
   }

   const container = document.getElementById('savingsTableContainer');
   container.innerHTML = '' //  rensa tabell först
   container.appendChild(table)
}