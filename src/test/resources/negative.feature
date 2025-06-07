Feature: Brand filtering - Negative Validation

  Scenario: Ensure only selected brand's products are shown and other brands are excluded
    Given I navigate to "https://casekaro.com/pages/mobile-back-covers"
    When I click on "Mobile Covers"
    And I search for brand "Apple"
    And I search for model "iPhone 16 pro"
    When I apply filter "Availability" with option "In stock"
    Then I fetch and store product details from first 2 pages
    Then I validate that products do not contain other brands like "Samsung, OnePlus, Vivo, Oppo"
