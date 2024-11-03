package lotto.controller;

import java.util.List;
import lotto.aop.RetryHandler;
import lotto.domain.Lotto;
import lotto.domain.LottoStatics;
import lotto.domain.Money;
import lotto.domain.Number;
import lotto.domain.WinningLotto;
import lotto.domain.numberPicker.NumberPicker;
import lotto.dto.IncomeStatics;
import lotto.dto.PrizeStatics;
import lotto.dto.PurchasedLottos;
import lotto.io.InputHandler;
import lotto.io.OutputHandler;

public class LottoApplicationController {

    private final InputHandler inputHandler;
    private final OutputHandler outputHandler;
    private final RetryHandler retryHandler;
    private final NumberPicker numberPicker;

    public LottoApplicationController(
            InputHandler inputHandler,
            OutputHandler outputHandler,
            RetryHandler retryHandler,
            NumberPicker numberPicker
    ) {
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
        this.retryHandler = retryHandler;
        this.numberPicker = numberPicker;
    }

    public void run() {
        Money money = retryHandler.tryUntilSuccess(() -> {
            int amount = inputHandler.handlePurchaseCost();
            return new Money(amount);
        });

        List<Lotto> purchasedLottos = Lotto.purchase(money, numberPicker);

        outputHandler.handlePurchasedLottos(PurchasedLottos.from(purchasedLottos));

        Lotto winningNumbers = retryHandler.tryUntilSuccess(() -> {
            List<Number> list = inputHandler.handleWinningNumbers().stream().map(Number::new).toList();
            return new Lotto(list);
        });

        WinningLotto winningLotto = retryHandler.tryUntilSuccess(() -> {
            int bonusNumber = inputHandler.handleBonusNumber();
            return new WinningLotto(winningNumbers, new Number(bonusNumber));
        });

        LottoStatics lottoStatics = new LottoStatics(purchasedLottos, winningLotto, money);

        outputHandler.handlePrizeStatics(PrizeStatics.from(lottoStatics));
        outputHandler.handleIncomeStatics(IncomeStatics.from(lottoStatics));
    }
}
