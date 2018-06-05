# blackjack

A simple implementation of [Blackjack] (https://en.wikipedia.org/wiki/Blackjack) written in [re-frame](https://github.com/Day8/re-frame).

Does not currently support naturals, double downs, splits, or insurance.

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
