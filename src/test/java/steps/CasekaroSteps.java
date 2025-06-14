package steps;

import com.microsoft.playwright.*;
import io.cucumber.java.en.*;
import java.util.*;

import org.junit.Assert;

import static org.junit.Assert.*;

public class CasekaroSteps {

    // These are some variables neccessory to store our data
    // browser variable is req to access the browser engine(here we will use
    // chromium)
    // page is required to navigate to the link
    // Since we can have multiple brands so that we are not bounded to test only for
    // one brand and can reuse code
    // for this we have used a variable brand of string type
    // model is the model of the mobile phone for which we are searching the covers
    // covers is a list of maps to store the details of each cover found on the

    public Browser browser;
    public Page page;
    public String category;
    public String brand;
    public String model;
    List<Map<String, String>> covers = new ArrayList<>();

    // So basically in below given function we are starting the playwright engine
    // and using chromium browser (base browser for chrome and some other browsers)
    // and then we are launching the browser in non-headless mode so that we can see
    // the actions performed by the code
    // and then we are creating a new page in the browser and navigating to the url
    // given in the feature file
    // If the url is not casekaro's url then we are failing the test case
    // and if the url is valid then we are navigating to that url

    @Given("I navigate to {string}")
    public void NavigateToUrl(String url) {
        if (!url.contains("casekaro.com")) {
            Assert.fail("Not casekaro's url.");
        }
        Playwright playwright = Playwright.create();
        BrowserType browserType = playwright.chromium();
        browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        page.navigate(url);
    }


    // In this step we are selecting the category of mobile covers
    // and if the category is not mobile covers then we are failing the test case

    @When ("I select the {string} category")
    public void SelectCategory(String category) {
        if (!(category.equals("Mobile Covers"))){
            Assert.fail("Not a valid category.");
        }
        this.category = category;
        page.click("#HeaderMenu-mobile-covers");
    }


    // In this step we are searching for the brand of mobile covers
    // and if the brand is not found then we are failing the test case

    @And ("I search for brand {string}")
    public void SearchBrand(String brand) {
        this.brand = brand;
        page.fill("#search-bar-cover-page", brand);

        Locator visibleBrands = page.locator(".brand-name-container[style*='display: block']");
        int visibleCount = visibleBrands.count();
        if (visibleCount == 0) {
        Assert.fail("Brand not found: " + brand);
    }
    if( visibleCount > 1) {
            Assert.fail("Multiple brands found ");
        }
        page = page.waitForPopup(() -> visibleBrands.locator("a").first().click());

page.waitForLoadState();
    }

    // In this step we are searching for the model of mobile covers
    // and if the model is not found then we are failing the test case

    @When ("I search for model {string}")
    public void SearchModel(String model) {
        this.model = model;
        page.fill("#search-bar-cover-page", model);
        Locator modelLink = page.locator("a:has-text(\"" + model + "\")").first();
        if (modelLink.count() == 0 || !modelLink.isVisible()) {
            Assert.fail("Model not found " + model);
        }
        modelLink.click();
        page.waitForLoadState();
    }

    // In this step we are applying the filter on the mobile covers
    // and if the filter is not found then we are failing the test case

    @When ("I apply filter {string} with option {string}")
    public void ApplyFilter(String category, String option){
        Locator filter = page.locator("summary:has-text('" + category + "')").first();
        filter.click();

        Locator dropdown = filter.locator("xpath=ancestor::details[@open]");
        dropdown.waitFor();

        Locator label = dropdown.locator("label:has-text('" + option + "')").first();
        label.click();
    }

    // In this step we are fetching the product details from the first n pages
    // and storing them in the covers list


    @Then("I fetch and store product details from first {int} pages")
public void FetchProductDetails(int pageCount) {
    for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
        page.waitForLoadState();
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

            String discounted = "";
            Locator discLoc = card.locator("span.price-item--sale");
            if (discLoc.count() > 0) {
                discounted = discLoc.first().innerText().replaceAll("[^0-9.]", "");
            }

            String actual = "";
            Locator actLoc = card.locator("s.price-item--regular");
            if (actLoc.count() > 0) {
                actual = actLoc.first().innerText().replaceAll("[^0-9.]", "");
            }

            String imgSrc = "";
            Locator imgLoc = card.locator("div.card__media img");
            if (imgLoc.count() > 0) {
                imgSrc = imgLoc.first().getAttribute("src");
                if (imgSrc != null && !imgSrc.startsWith("http")) {
                    imgSrc = "https:" + imgSrc;
                }
            }

            Map<String, String> product = new HashMap<>();
            product.put("description", description);
            product.put("discounted", discounted);
            product.put("actual", actual);
            product.put("imgSrc", imgSrc);

            covers.add(product);
        }

        if (pageNum < pageCount) {
            Locator openFilters = page.locator("aside#main-collection-filters details[open] > summary.facets__summary");
            int openCount = openFilters.count();
            for (int k = 0; k < openCount; k++) {
                Locator openFilter = openFilters.nth(k);
                if (openFilter.isVisible()) {
                    openFilter.click();
                    page.waitForTimeout(200);
                }
            }

            Locator nextBtn = page.locator("a[aria-label='Next page']");
            if (nextBtn.count() > 0 && nextBtn.first().isVisible()) {
                nextBtn.first().click();
                page.waitForLoadState();
            } else {
                System.out.println("Next button not found on page " + pageNum);
                break;
            }
        }
    }

    assertFalse(covers.isEmpty());
}

    // In this step we are sorting the product by discounted price in ascending order
    // and if no products are found then we are failing the test case

    @Then("I sort the product in asending order by discounted price")
    public void SortByDiscountedPrice() {
        if (covers.isEmpty()) {
            Assert.fail("No products found to sort.");
            return;
        }
        int n = covers.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                double price1 = Double.parseDouble(covers.get(j).get("discounted"));
                double price2 = Double.parseDouble(covers.get(j + 1).get("discounted"));
                if (price1 > price2) {
                    Map<String, String> temp = covers.get(j);
                    covers.set(j, covers.get(j + 1));
                    covers.set(j + 1, temp);
                }
            }
        }
    }

    // In this step we are printing the sorted data to the console
    

    @Then("I print the sorted data to the console")
    public void PrintSortedData() {
        if (covers.isEmpty()) {
            Assert.fail("No covers found to print.");
            return;
        }
        System.out.println("Sorted Covers by Discounted Price:");
        for (Map<String, String> cover : covers) {
            System.out.println("Description: " + cover.get("description"));
            System.out.println("Discounted Price: " + cover.get("discounted"));
            System.out.println("Actual Price: " + cover.get("actual"));
            System.out.println("Image Source: " + cover.get("imgSrc"));
            System.out.println("-----------------------------");
        }
        browser.close();
    }
}

// Thankyou Gocomet team for your time and consideration
// I am so excited to work with you and grow within this organization and with
// the organization
