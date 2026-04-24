// require("./instrument"); // MUST be first

const fs = require('fs');
const path = require('path');

const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const morgan = require('morgan');
// const Sentry = require("@sentry/node");

const Goal = require('./models/goal');

const app = express();

// Sentry request handler (must be before routes)
// app.use(Sentry.Handlers.requestHandler());

const accessLogStream = fs.createWriteStream(
  path.join(__dirname, 'logs', 'access.log'),
  { flags: 'a' }
);

app.use(morgan('combined', { stream: accessLogStream }));
app.use(bodyParser.json());

app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  next();
});

// ------------------- ROUTES -------------------

async function fetchGoals(req, res) {
  console.log('TRYING TO FETCH GOALS');
  try {
    const goals = await Goal.find();
    res.status(200).json({
      goals: goals.map((goal) => ({
        id: goal.id,
        text: goal.text,
      })),
    });
    console.log('FETCHED GOALS');
  } catch (err) {
    // Sentry.captureException(err); // Capture handled error
    console.error('ERROR FETCHING GOALS');
    console.error(err.message);
    res.status(500).json({ message: 'Failed to load goals.' });
  }
}

async function saveGoal(req, res) {
  console.log('TRYING TO STORE GOAL');
  const goalText = req.body.text;

  if (!goalText || goalText.trim().length === 0) {
    console.log('INVALID INPUT - NO TEXT');
    return res.status(422).json({ message: 'Invalid goal text.' });
  }

  const goal = new Goal({
    text: goalText,
  });

  try {
    await goal.save();
    res.status(201).json({
      message: 'Goal saved',
      goal: { id: goal.id, text: goalText },
    });
    console.log('STORED NEW GOAL');
  } catch (err) {
    // Sentry.captureException(err); // Capture handled error
    console.error('ERROR SAVING GOAL');
    console.error(err.message);
    res.status(500).json({ message: 'Failed to save goal.' });
  }
}

async function deleteGoal(req, res) {
  console.log('TRYING TO DELETE GOAL');
  try {
    await Goal.deleteOne({ _id: req.params.id });
    res.status(200).json({ message: 'Deleted goal!' });
    console.log('DELETED GOAL');
  } catch (err) {
    // Sentry.captureException(err); // Capture handled error
    console.error('ERROR DELETING GOAL');
    console.error(err.message);
    res.status(500).json({ message: 'Failed to delete goal.' });
  }
}

app.get('/goals', fetchGoals);
app.get('/api/goals', fetchGoals);

app.post('/goals', saveGoal);
app.post('/api/goals', saveGoal);

app.delete('/goals/:id', deleteGoal);
app.delete('/api/goals/:id', deleteGoal);

// Temporary test route (for verifying Sentry integration)
// app.get('/sentry-test', (req, res) => {
//   throw new Error("Sentry Integration Test 🚨");
// });

// Sentry error handler (must be after routes)
// Sentry.setupExpressErrorHandler(app);


// ------------------- DATABASE -------------------

mongoose.connect(
  process.env.MONGODB_URL || 'mongodb://mongodb:27017/course-goals',
  {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  },
  (err) => {
    if (err) {
      console.error('FAILED TO CONNECT TO MONGODB');
      console.error(err);
    } else {
      console.log('CONNECTED TO MONGODB');
      app.listen(80);
    }
  }
);
