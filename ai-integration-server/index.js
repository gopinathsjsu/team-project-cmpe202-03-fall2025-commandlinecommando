const express = require('express');
const cors = require('cors');
const OpenAI = require('openai');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

function getOpenAIClient() {
  if (!process.env.OPENAI_API_KEY) {
    return null;
  }
  return new OpenAI({
    apiKey: process.env.OPENAI_API_KEY,
  });
}

app.post('/api/chat', async (req, res) => {
  try {
    const { messages, listingsContext, reportsContext } = req.body;

    const openai = getOpenAIClient();
    if (!openai) {
      return res.status(500).json({ 
        error: 'OpenAI API key is not configured. Please add OPENAI_API_KEY to your secrets.' 
      });
    }

    let systemPrompt = `You are a helpful AI assistant for CampusConnect, a university campus marketplace. You help students find products, answer questions about available listings, and provide information about reported listings.

Here are the current marketplace listings:
${listingsContext}`;

    if (reportsContext && reportsContext.trim()) {
      systemPrompt += `

Here are the reported listings (these are listings that users have flagged for various reasons):
${reportsContext}

Report types include: SPAM, INAPPROPRIATE, SCAM, WRONG_CATEGORY
Report statuses include: PENDING, UNDER_REVIEW, RESOLVED, DISMISSED`;
    }

    systemPrompt += `

Based on this data, answer the user's questions helpfully. If they ask about products, prices, or recommendations, use the listing data to provide accurate answers. If they ask about reported listings or reports, provide information from the reports data. Format your responses nicely with clear information. If a question cannot be answered with the available data, let them know politely.`;

    const completion = await openai.chat.completions.create({
      model: 'gpt-4o-mini',
      messages: [
        { role: 'system', content: systemPrompt },
        ...messages,
      ],
      temperature: 0.7,
      max_tokens: 1000,
    });

    const assistantMessage = completion.choices[0]?.message?.content || 'Sorry, I could not generate a response.';

    res.json({ message: assistantMessage });
  } catch (error) {
    console.error('OpenAI API Error:', error);
    res.status(500).json({ 
      error: error.message || 'Failed to get AI response' 
    });
  }
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', hasApiKey: !!process.env.OPENAI_API_KEY });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on port ${PORT}`);
});
