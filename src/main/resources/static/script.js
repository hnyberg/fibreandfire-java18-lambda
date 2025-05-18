let stockChart = null;

document.getElementById("dataForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const formData = new FormData(e.target)
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
          "percentForAmortization": Number(formData.get("percent_for_amortization"))
        }
      };

      const formDataAsString = JSON.stringify(formDataAsJson)

      console.log("sending data:", JSON.stringify(formDataAsJson))

      const baseUrl = location.hostname === 'localhost'
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
        const result = { ...jsonResponse }
        delete result.monthly_data

        document.getElementById("json-result").innerHTML = `
          <h3>Resultat</h3>
          <u1>
            <li><strong>Nödkonto fyllt:</strong>${formatDate(result.emergencyFilledDate)}</li>
            <li><strong>CSN avbetalt:</strong>${formatDate(result.csnFreeDate)}</li>
            <li><strong>Bostadslån betalat:</strong>${formatDate(result.mortgageFreeDate)}</li>
            <li><strong>FIRE-tid:</strong>${formatDate(result.fireDate)}</li>
            <li><strong>FIRE-summa (kr):</strong>${Math.round(result.fireAmount).toLocaleString('sv-SE')}</li>
          </u1>
        `

        const monthlyData = jsonResponse.monthlyData
        const labels = monthlyData.map(row => row.date)
        const age = monthlyData.map(row => row.age)
        const stockSavings = monthlyData.map(row => row.stockSavings)
        const mortgageLeft = monthlyData.map(row => row.mortgageLeft)
        const csnLeft = monthlyData.map(row => row.csnLeft)
        const fireAmount = monthlyData.map(row => row.fireAmount)
        const stockChartElement = document.getElementById('stockChart')

        if (stockChart) {
            stockChart.data.labels = labels;
            stockChart.data.datasets[0].data = stockSavings
            stockChart.data.datasets[1].data = mortgageLeft
            stockChart.data.datasets[2].data = csnLeft
            stockChart.data.datasets[3].data = fireAmount
            stockChart.update()
        } else {
            stockChart = new Chart(stockChartElement, {
                type: 'line',
                data: {
                    labels,
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

      } else {
        console.error("Något gick fel:", response.status)
      }

    });

    function formatDate(date) {
        const [year, month, day] = date.split("-")
        return `${year}-${month}`
    }