Feature: Positive scenarios for CashKaro Product Search

  Scenario: Search Apple mobile covers and filter In Stock
    Given I navigate to "<links>"
    When I select the "Mobile Covers" category
    And I search for brand "<brand>"
    When I search for model "<model>"
    When I apply filter "Availability" with option "In stock"
    Then I fetch and store product details from first <pages> pages
    Then I sort the product in asending order by discounted price
    Then I print the sorted data to the console

Examples:
    | model         | brand  | links                 | pages |
    | iPhone 16 pro | Apple  | https://casekaro.com/ | 2     |
    