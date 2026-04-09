# PayU

PayU is a modern Android personal finance app for tracking income, expenses, payment methods, balances, and spending patterns. It is built with Jetpack Compose and designed to make day-to-day money management feel visual, fast, and easy to understand.



## What PayU Does

The app centers around a few core ideas:

- track every transaction
- attach each transaction to a bank account or card
- keep balances for each payment method up to date
- visualize spending over time
- show a simple financial health score

## Project Setup

### Requirements

- Android Studio
- JDK 11 or later
- Android SDK matching the project setup

### How to run

1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Run the app on an emulator or physical device.

### Notes

- The app currently uses local storage for transactions, profiles, and payment methods.
- No backend setup is required.

## Screenshots

Place your screenshots in a folder named `screenshots` at the project root.

Example:

```text
payU/
  screenshots/
    01-home.png
    02-hero-card.png
    03-add-transaction.png
    04-calendar.png
    05-profile.png
```

Then reference them in this section like this:

```md
![Home Screen](screenshots/01-home.png)
![Hero Card](screenshots/02-hero-card.png)
![Add Transaction](screenshots/03-add-transaction.png)
![Calendar Heatmap](screenshots/04-calendar.png)
![Profile Screen](screenshots/05-profile.png)
```

## Feature List

### 1. Home Dashboard

The home screen is the main financial overview screen. It combines spending summaries, payment method cards, and transaction history in one place.

What it does:

- shows a greeting and rotating hint text
- displays a horizontally scrollable hero card carousel for all payment methods
- lets you flip a card to see back-side details
- lets you double tap a card to add or remove that method as a filter
- highlights the centered card with a smoother, softer animation
- shows recent, weekly, or monthly spending summaries
- allows a payment-method drawer to slide open from the top
- lets you set any payment method as primary
- lets you add a new payment method from the drawer

The hero cards are designed to feel like real payment cards:

- front side shows bank logo, bank name, account holder, total balance, last 4 digits, and a virtual-card label
- back side shows account/card number, CVV, expiry date, and total spent for that method
- each card has a glare/shimmer animation and a 3D flip rotation

### 2. Payment Methods Drawer

The top drawer gives a quick summary of all linked payment methods.

It includes:

- total balance across all payment methods
- a list of linked accounts/cards
- the ability to set a primary payment method
- a button to add a new payment method
- draggable open/close behavior
- tap outside to dismiss

The drawer is also connected to the same bank-logo system used throughout the app.

### 3. Add Transaction Screen

The add transaction screen is designed for quick entry and clear payment tagging.

It supports:

- expense and income mode
- amount entry
- note/description entry
- date selection
- payment method selection
- automatic defaulting to the primary payment method if you do not choose one
- category selection with a large grid
- a fixed bottom save button

Transaction details are stored with:

- category
- amount
- note
- date
- transaction type
- selected payment method

### 4. Payment Method Selection

Users can link multiple payment methods, including:

- bank accounts
- cards
- cash

Each payment method stores:

- bank name
- last 3 to 4 digits
- current balance
- primary/secondary status
- account number
- card number
- CVV
- expiry date

The additional card/account fields are optional. If the user leaves them blank, the app stores masked placeholders so the UI still has consistent data to show.

### 5. Profile Screen

The profile screen is focused on account information and payment-method management.

It includes:

- animated preview/edit chips similar to the dashboard
- total spendings
- total balance across all payment methods
- linked payment method count
- primary payment method display
- a simple payment score/health-style metric
- dark mode toggle
- payment-method management list
- edit balance support for each payment method
- edit details for bank number, card number, CVV, and expiry
- sign out button at the bottom of the screen

The profile payment-method cards are aligned to the same style used in the top drawer, so the experience stays visually consistent.

### 6. Calendar Screen

The calendar screen focuses on spending over time.

It includes:

- a heatmap of monthly spending activity
- a horizontal date scroller above the heatmap
- tap to inspect expenses for a selected day
- the selected day only shows expenses from the current month
- a day-level expense list below the heatmap

This screen is intended to help users spot spending patterns quickly.

### 7. Analytics / Balances

The bottom navigation includes an analytics-focused screen for financial insights.

This area is intended for:

- summary metrics
- balance insights
- income and expense trends
- category-level analysis

### 8. Transaction History

Transaction history is embedded into the dashboard and is designed for fast review.

It supports:

- filtering by recent, weekly, or monthly views
- filtering by payment method
- bank-logo badges on each transaction row
- search across transaction notes, categories, and amounts
- swipe to delete
- undo via snackbar

### 9. Category Suggestions

The app includes a lightweight category classifier that can suggest a category based on the note/description you type.

If the note is long enough, the app can suggest a category and let you tap to apply it.

### 10. Authentication and Theme

The app includes:

- splash screen
- login/auth flow
- dark mode support
- theme persistence through user preferences

## Finance Score

The app calculates a financial health score in the `0-100` range.

In simple terms, the score rewards:

- stronger savings
- healthier balances
- more organized payment-method usage

and penalizes:

- higher expense pressure
- weak or negative savings

### Inputs used

The score uses these values:

- total income
- total expense
- total balance
- number of payment methods

### How it works

The logic starts with a baseline:

- if the user has no activity at all, the score returns a neutral midpoint of `50`
- otherwise, the score is built from a combination of savings behavior, balance quality, payment-method diversity, and expense pressure

### Breakdown

1. **Savings rate**
   - If income exists, the score looks at how much of that income is left after expenses.
   - Higher savings increase the score.
   - Overspending lowers the score.

2. **Balance factor**
   - The app compares total balance, income, and expenses to see whether the account state looks healthy overall.
   - Strong positive balances improve the score.

3. **Payment method factor**
   - The app gives a small bonus for having multiple linked payment methods, up to a limit.
   - This rewards users for organizing finances across cards/accounts instead of keeping everything in one place.

4. **Expense pressure**
   - If income exists, the app checks how much of it is being spent.
   - A higher spend-to-income ratio reduces the score.

### Final score range

The result is clamped to stay between:

- `0` and `100`

### Practical interpretation

- `80-100`: healthy finances and good control
- `60-79`: decent management with some room to improve
- `40-59`: average or unstable
- `0-39`: high expense pressure or weak balance health

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore Preferences
- Navigation Compose
- Kotlin Coroutines / Flow
- Compose animation APIs

## Detailed Screens

### Home Screen

- greeting with rotating hint text
- payment method hero carousel with flip animation
- double tap to filter by payment method
- draggable payment-method drawer
- recent, weekly, and monthly transaction views
- transaction search
- swipe-to-delete with undo

### Add Transaction Screen

- expense/income selector
- amount entry
- note and category input
- date picker
- payment method selector with default primary selection
- fixed save button

### Calendar Screen

- monthly heatmap
- date scroller
- day-specific expense inspection

### Profile Screen

- preview/edit modes
- payment method management
- balance editing
- profile analytics
- dark mode toggle
- sign out

### Analytics Screen

- financial overview
- balance insights
- spending analysis

## Architecture

PayU follows a fairly clean MVVM-style structure:

- **UI layer**: Compose screens and reusable components
- **ViewModels**: state, validation, filtering, and business rules
- **Repository layer**: transaction storage and queries
- **Local data layer**: Room database and DAO
- **Preferences layer**: user profile, theme, and payment methods

### Main data flow

1. User interacts with a Compose screen.
2. The screen calls the relevant ViewModel.
3. The ViewModel updates Room or DataStore.
4. The UI reacts automatically through Flow/StateFlow.

## Project Structure

```text
app/src/main/java/com/sarthak/payu/
  data/
    local/
    model/
    repo/
  di/
  ui/
  utils/
  view/
    components/
    navigation/
    screen/
  vm/
```

## Getting Started

### Requirements

- Android Studio
- JDK 11 or later
- Android SDK matching the project setup

### Run locally

1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Run the app on an emulator or Android device.

### Notes

- The app uses local storage for transactions and payment methods.
- No external backend is required for the current version.

## Screenshot Placement Suggestion

Once you add screenshots, a good ordering is:

1. Home dashboard
2. Hero card flip
3. Add transaction screen
4. Payment methods drawer
5. Calendar heatmap
6. Profile screen

## Future Ideas

Possible next steps for PayU:

- calendar month navigation
- better analytics charts
- category-wise spending insights
- export transactions
- recurring expense tracking
- richer financial score explanations

## License

Add a license here if you want to publish the project publicly.
