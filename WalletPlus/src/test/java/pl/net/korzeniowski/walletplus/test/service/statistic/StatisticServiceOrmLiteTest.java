package pl.net.korzeniowski.walletplus.test.service.statistic;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import pl.net.korzeniowski.walletplus.dagger.test.ServiceInjectedUnitTest;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.Wallet;

import static org.assertj.core.api.Assertions.assertThat;

@SmallTest
public class StatisticServiceOrmLiteTest extends ServiceInjectedUnitTest{

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void shouldCountCashFlowsAssignedToWallet() {
        // given
        Wallet wallet1 = new Wallet().setName("Wallet 1").setInitialAmount(100.0);
        walletService.insert(wallet1);
        Wallet wallet2 = new Wallet().setName("Wallet 2").setInitialAmount(200.0);
        walletService.insert(wallet2);

        CashFlow cashFlow = new CashFlow()
                .setAmount(50.0)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));

        // then
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet1.getId())).isEqualTo(2);
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet2.getId())).isEqualTo(3);
    }

    @Test
    public void shouldCountCashFlowsAssignedToTag() {
        // given
        Wallet wallet = new Wallet().setName("Wallet").setInitialAmount(100.0);
        walletService.insert(wallet);
        Tag tag1 = new Tag("tag1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("tag2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("tag3");
        tagService.insert(tag3);

        CashFlow cashFlow = new CashFlow()
                .setAmount(50.0)
                .setWallet(wallet)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow.setId(null).clearTags().setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag3).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag2).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag3).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2, tag3).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag2, tag3).setType(CashFlow.Type.INCOME));

        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2, tag3).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag3).setType(CashFlow.Type.INCOME));

        // then
        assertThat(statisticService.countCashFlowsAssignedToTag(tag1.getId())).isEqualTo(4);
        assertThat(statisticService.countCashFlowsAssignedToTag(tag2.getId())).isEqualTo(5);
        assertThat(statisticService.countCashFlowsAssignedToTag(tag3.getId())).isEqualTo(6);
    }
}
