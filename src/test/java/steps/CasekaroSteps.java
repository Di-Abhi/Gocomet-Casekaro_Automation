package steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import java.util.*;

public class CasekaroSteps {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;
    List<Product> products = new ArrayList<>();

    // created a class which hold product details
    static class Product {
        private final double discountedPrice;
        private final double actualPrice;
        private final String description;
        private final String imageLink;

        public Product(double discountedPrice, double actualPrice, String description, String imageLink) {
            this.discountedPrice = discountedPrice;
            this.actualPrice = actualPrice;
            this.description = description;
            this.imageLink = imageLink;
        }

        public double getDiscountedPrice() { return discountedPrice; }
        public double getActualPrice() { return actualPrice; }
        public String getDescription() { return description; }
        public String getImageLink() { return imageLink; }

        @Override
        public String toString() {
            return String.format(
                "Description: %s%nDiscounted Price: ₹%.2f%nActual Price: ₹%.2f%nImage Link: %s%n%s",
                description, discountedPrice, actualPrice, imageLink,
                "---------------------------------------"
            );
        }
    }

    // this fuction create browser and page before each scenario
    @Before
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    // when the testing is over this function will close the browser playwright etc.
    @After
    public void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }

    // Step to open a given URL and check the title
    @Given("I navigate to {string}")
    public void i_navigate_to(String url) {
        page.navigate(url);
        Assertions.assertTrue(page.title().toLowerCase().contains("casekaro"),
                "Page title must contain 'casekaro'");
    }

    // this function to click on a menu option
    @When("I click on {string}")
    public void i_click_on(String menuText) {
        if (menuText.equals("Mobile Covers")) {
            page.navigate("https://casekaro.com/pages/mobile-back-covers");
            page.waitForLoadState();
            Assertions.assertTrue(page.url().toLowerCase().contains("casekaro.com"),
                    "URL should contain casekaro.com");
        } else {
            Locator menu = page.locator("nav a:has-text('" + menuText + "'), header a:has-text('" + menuText + "')").first();
            menu.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            menu.click();
            Assertions.assertTrue(page.url().toLowerCase().contains(menuText.toLowerCase().replace(" ", "-")),
                    "URL should contain " + menuText.toLowerCase().replace(" ", "-"));
        }
    }

    // this function is use to search for a brand
    @When("I search for brand {string}")
    public void i_search_for_brand(String brand) {
        Locator input = page.locator("#search-bar-cover-page");
        input.click();
        input.fill(brand);

        Locator brandSuggestion = page.locator("button.brand-name-container >> text=" + brand);
        brandSuggestion.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        Page newPage = page.waitForPopup(() -> {
            brandSuggestion.click();
        });
        this.page = newPage;
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // this function is use to to search for a specific model
    @When("I search for model {string}")
    public void i_search_for_model(String model) {
        Locator input = page.locator("#search-bar-cover-page");
        input.click();
        input.fill(model);

        Locator modelLink = page.locator("a:has-text(\"" + model + "\")").first();
        modelLink.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        modelLink.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    //this function is use to apply a filter on the page
    @When("I apply filter {string} with option {string}")
    public void i_apply_filter_with_option(String filterCategory, String optionText) {
        Locator summary = page.locator("summary:has-text('" + filterCategory + "')").first();
        summary.scrollIntoViewIfNeeded();
        summary.click();

        Locator dropdown = summary.locator("xpath=ancestor::details[@open]");
        dropdown.waitFor();

        Locator label = dropdown.locator("label:has-text('" + optionText + "')").first();
        label.scrollIntoViewIfNeeded();
        label.click();

        Locator checkbox = label.locator("input[type='checkbox']");
        Assertions.assertTrue(checkbox.isChecked(), optionText + " checkbox should be checked");
    }

    // this function is use to verify products do not include any other brands
    @Then("I validate that products do not contain other brands like {string}")
    public void i_validate_that_products_do_not_contain_other_brands_like(String commaSeparatedBrands) {
        String[] disallowedBrands = commaSeparatedBrands.split(",");

        Locator productCards = page.locator("li.grid__item"); 
        int cardCount = productCards.count();

        for (int i = 0; i < cardCount; i++) {
            Locator card = productCards.nth(i);
            String description = "";
            Locator descLoc = card.locator("h3.card__heading a");
            if (descLoc.count() > 0) {
                description = descLoc.first().innerText().toLowerCase();
            }

            for (String brand : disallowedBrands) {
                String disallowedBrand = brand.trim().toLowerCase();
                Assertions.assertFalse(description.contains(disallowedBrand),
                        String.format("Product '%s' should not contain disallowed brand '%s'", description, disallowedBrand));
            }
        }
    }

    // this function is use to collect and store product data from multiple pages
    @Then("I fetch and store product details from first {int} pages")
    public void i_fetch_and_store_product_details_from_first_pages(Integer pageCount) {
        products.clear();

        String baseUrl = page.url();
        baseUrl = baseUrl.replaceAll("[&?]page=\\d+", "");

        for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
            String pageUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "page=" + pageNum;
            page.navigate(pageUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForSelector("li.grid__item");

            Locator productCards = page.locator("li.grid__item");
            int cardCount = productCards.count();

            for (int i = 0; i < cardCount; i++) {
                Locator card = productCards.nth(i);

                String description = "";
                Locator descLoc = card.locator("h3.card__heading a");
                if (descLoc.count() > 0) {
                    description = descLoc.first().innerText().trim();
                }

                String discountedStr = "";
                Locator discLoc = card.locator("span.price-item--sale");
                if (discLoc.count() > 0) {
                    discountedStr = discLoc.first().innerText().replaceAll("[^0-9.]", "");
                }

                String actualStr = "";
                Locator actLoc = card.locator("s.price-item--regular");
                if (actLoc.count() > 0) {
                    actualStr = actLoc.first().innerText().replaceAll("[^0-9.]", "");
                }

                String imgSrc = "";
                Locator imgLoc = card.locator("div.card__media img");
                if (imgLoc.count() > 0) {
                    imgSrc = imgLoc.first().getAttribute("src");
                    if (imgSrc != null && !imgSrc.startsWith("http")) {
                        imgSrc = "https:" + imgSrc;
                    }
                }

                double discounted = discountedStr.isEmpty() ? 0.0 : Double.parseDouble(discountedStr);
                double actual = actualStr.isEmpty() ? discounted : Double.parseDouble(actualStr);

                products.add(new Product(discounted, actual, description, imgSrc));
            }
        }

        Assertions.assertFalse(products.isEmpty(), "Product list should not be empty");
    }

    // Step to print all product details sorted by discounted price
    @Then("I print all products sorted by discounted price ascending")
    public void i_print_all_products_sorted_by_discounted_price_ascending() {
        products.sort(Comparator.comparingDouble(Product::getDiscountedPrice));

        for (Product p : products) {
            System.out.println(p);
        }
    }
}

// Thankyou Gocomet team for your time and consideration
// I am so excited to work with you and grow within this organization and with
// the organization

//The problem I have faced in this project was for fatching the product details from the first 2 pages
// I have used the page.waitForSelector("li.grid__item") to wait for the product
// cards to be visible before fetching the details.
// May be there exists a better way to get rid of this issue, I am excited to
// learn that

// I have listed both positive and negative test Scenarios in different feature
// files, where we can use multiple test cases
// of both the scenarios using example of brand name and model name

// About negative test cases: it will search for given brand name and given model
//and it will insure any other brand product is not visible

// About Positive test cases: Valid usage (excluding practices described in neg
// testcases) will pass the testcase.
// Assertions: We have used them whenever we found our project not to work
// properly where it should work.
