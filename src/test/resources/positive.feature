Feature: Positive scenarios for CashKaro Product Search

  Scenario: Search Apple mobile covers and filter In Stock
    Given I navigate to "https://casekaro.com/"
    When I click on "Mobile Covers"
    And I search for brand "Apple"
    When I search for model "iPhone 16 pro"
    When I apply filter "Availability" with option "In stock"
    Then I fetch and store product details from first 2 pages
    And I print all products sorted by discounted price ascending
