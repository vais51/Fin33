package com.anjlab.fin33.model;

import com.crashlytics.android.Crashlytics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Саня on 16.09.2016.
 */
public class Fin33Parser {

    public void parseMainInfo(final Document doc, final BanksUpdatedListener listener) throws ParseException {


        //List<ExchangeRate> exchangeRates = new ArrayList<>();
//       Thread downloadThread =  new Thread () { //убрать поток
//           @Override
//           public void run() {

        final List<Bank> banks = new ArrayList<>();
        Elements trs = doc.select("table.otscourses tr");
        trs.remove(0);
        trs.remove(0);
        for (Element tr : trs) {
            Bank bank = new Bank();
            Date docDate = null;
            Elements tds = tr.getElementsByTag("td");
            Element td = tds.get(0); // Получаем первый td  и все его дочерние эллементы
            Element a = td.select("a").first();// Получаем ссылку банка
            a.attr("href");
            bank.setName(a.text()); // Запоминаем  имя банка
            Element sup = td.select("sup").first();
            if (sup.children().size() == 1) {
                String dateStr = sup.child(0).text();
                SimpleDateFormat format = new SimpleDateFormat();
                format.applyPattern("dd.MM.yyyy");

                try {
                    docDate = format.parse(dateStr);
                } catch (ParseException e) {
                    Crashlytics.logException(new RuntimeException("This is a parseException"));
                    e.printStackTrace();
                }

            } else if (sup.children().size() == 2) {
                //String toDay = sup.child(0).text();
                String timeStr = sup.child(1).text();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                Calendar calendar = new GregorianCalendar();
                int yyyy = calendar.get(Calendar.YEAR);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                int mm = calendar.get(Calendar.MONTH) + 1;
                try {
                    docDate = format.parse(dd + "." + mm + "." + yyyy + " " + timeStr);
                } catch (ParseException e) {
                    Crashlytics.logException(new RuntimeException("This is a parseException"));
                    e.printStackTrace();
                }
            }
            bank.addExchangeRate(createExchangeRateFrom(tds.get(1), ExchangeRate.Kind.BUY, ExchangeRate.Currency.USD, docDate, bank));
            bank.addExchangeRate(createExchangeRateFrom(tds.get(2), ExchangeRate.Kind.SELL, ExchangeRate.Currency.USD, docDate, bank));
            bank.addExchangeRate(createExchangeRateFrom(tds.get(3), ExchangeRate.Kind.BUY, ExchangeRate.Currency.EUR, docDate, bank));
            bank.addExchangeRate(createExchangeRateFrom(tds.get(4), ExchangeRate.Kind.SELL, ExchangeRate.Currency.EUR, docDate, bank));
            banks.add(bank);
            listener.onParseDone(banks);
        }
    }

    public ExchangeRate createExchangeRateFrom(Element td, ExchangeRate.Kind kind, ExchangeRate.Currency currency, Date docDate, Bank bank){
        ExchangeRate exchangeRate = new ExchangeRate();
        String str=td.text().replaceAll(",","");
        BigDecimal bd=new BigDecimal(str);
        if(td.hasClass("rate_up")){
            exchangeRate.setTrend(ExchangeRate.Trend.UP);
        } else if(td.hasClass("rate_down")){
            exchangeRate.setTrend(ExchangeRate.Trend.DOWN);
        }else {
            exchangeRate.setTrend(ExchangeRate.Trend.NONE);
        }
        exchangeRate.setBest(td.hasClass("best"));
        exchangeRate.setKind(kind);
        exchangeRate.setCurrency(currency);
        exchangeRate.setPrice(bd);
        exchangeRate.setDate(docDate);
        exchangeRate.setBank(bank);
        return exchangeRate;
    }

    public Document getDocument() throws IOException {
        return Jsoup.connect("http://www.fin33.ru/").get();
    }
}