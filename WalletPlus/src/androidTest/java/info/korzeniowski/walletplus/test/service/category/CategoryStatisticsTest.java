package info.korzeniowski.walletplus.test.service.category;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryStatisticsTest {

    @Inject
    @Named("local")
    CategoryService categoryService;

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Inject
    @Named("local")
    WalletService walletService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldReturnProperCategoryStats() {
        Category category = new Category().setName("Test category");
        categoryService.insert(category);
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 1"));
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 2"));

        Date today = DateTime.now().toDate();
        Date yesterday = new DateTime(today).minusDays(1).toDate();
        Date tomorrow = new DateTime(today).plusDays(1).toDate();

        // yesterday
        // -5 +7 -1 = +1
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(5.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(7.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(1.0).setDateTime(yesterday));

        // today
        // +50 +70 -90 = +30
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(0)).setAmount(50.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(70.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(90.0).setDateTime(today));

        // tomorrow
        // +100 -300 +700 = 500
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(100.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(300.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(700.0).setDateTime(tomorrow));

        CategoryService.CategoryStats stats = null;

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, yesterday, Period.days(1), 0));

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(1), 1);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, yesterday, Period.days(1), 1));

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(31.0);
        assertThat(stats.getFlow()).isEqualTo(223.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, yesterday, Period.days(2), 0));

        stats = categoryService.getCategoryStats(category, today, Period.days(1), -1);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, today, Period.days(1), -1));

        stats = categoryService.getCategoryStats(category, today, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, today, Period.days(1), 0));

        stats = categoryService.getCategoryStats(category, today, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(530.0);
        assertThat(stats.getFlow()).isEqualTo(1310.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(category, today, Period.days(2), 0));
    }

    @Test
    public void shouldReturnProperCategoryStatsForMain() {
        Category mainCategory = new Category().setName("Main category");
        categoryService.insert(mainCategory);
        Category subCategory = new Category().setParent(mainCategory).setName("Sub category");
        categoryService.insert(subCategory);
        Wallet myWallet = new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet");
        walletService.insert(myWallet);

        Date today = DateTime.now().toDate();

        cashFlowService.insert(new CashFlow().setCategory(mainCategory).setFromWallet(walletService.getMyWallets().get(0)).setAmount(3.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(subCategory).setToWallet(walletService.getMyWallets().get(0)).setAmount(5.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(subCategory).setFromWallet(walletService.getMyWallets().get(0)).setAmount(7.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(mainCategory).setToWallet(walletService.getMyWallets().get(0)).setAmount(11.0).setDateTime(today));

        CategoryService.CategoryStats mainCategoryStats = categoryService.getCategoryStats(mainCategory, today, Period.days(1), 0);
        assertThat(mainCategoryStats.getDifference()).isEqualTo(8.0);
        assertThat(mainCategoryStats.getTotalDifference()).isEqualTo(6.0);
        assertThat(mainCategoryStats.getFlow()).isEqualTo(14.0);
        assertThat(mainCategoryStats.getTotalFlow()).isEqualTo(26.0);
        assertThat(mainCategoryStats).isEqualTo(getCategoryStatsFromCategoryStateList(mainCategory, today, Period.days(1), 0));

        CategoryService.CategoryStats subCategoryStats = categoryService.getCategoryStats(subCategory, today, Period.days(1), 0);
        assertThat(subCategoryStats.getDifference()).isEqualTo(-2.0);
        assertThat(subCategoryStats.getTotalDifference()).isEqualTo(-2.0);
        assertThat(subCategoryStats.getFlow()).isEqualTo(12.0);
        assertThat(subCategoryStats.getTotalFlow()).isEqualTo(12.0);
        assertThat(subCategoryStats).isEqualTo(getCategoryStatsFromCategoryStateList(subCategory, today, Period.days(1), 0));
    }

    private CategoryService.CategoryStats getCategoryStatsFromCategoryStateList(final Category category, Date yesterday, Period period, Integer iteration) {
        List<CategoryService.CategoryStats> categoryStatsList = categoryService.getCategoryStatsList(yesterday, period, iteration);

        CategoryService.CategoryStats categoryStat = Iterables.find(categoryStatsList, new Predicate<CategoryService.CategoryStats>() {
            @Override
            public boolean apply(CategoryService.CategoryStats input) {
                return category.getId().equals(input.getCategoryId());
            }
        });
        return categoryStat;
    }
}
