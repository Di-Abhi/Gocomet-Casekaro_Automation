Feature: Negative scenarios for CashKaro Product Search

  Scenario: Search for unavailable mobile cover model and apply In Stock filter
    Given I navigate to "<links>"
    When I select the "Mobile Covers" category
    And I search for brand "<brand>"
    When I search for model "<model>"
    When I apply filter "Availability" with option "In stock"
    Then I fetch and store product details from first <pages> pages
    Then I sort the product in asending order by discounted price
    Then I print the sorted data to the console

  Examples:
    | model           | brand  | links                 | pages |
    | iPhone 99 ultra | HP  | https://casekaro.com/ | 2 |
    | iPhone 99 ultra | Apple  | https://casekaro.com/ | 2 |