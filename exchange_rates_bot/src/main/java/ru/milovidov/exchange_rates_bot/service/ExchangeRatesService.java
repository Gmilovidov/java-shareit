package ru.milovidov.exchange_rates_bot.service;

import ru.milovidov.exchange_rates_bot.ServiceException;

public interface ExchangeRatesService {
    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;
}
