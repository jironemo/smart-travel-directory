import 'package:flutter/material.dart';

class TripAdviceScreen extends StatelessWidget {
  const TripAdviceScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Trip Advice'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: ListView(
          children: const [
            Text(
              'Travel Tips:',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 16),
            Text('• Plan your itinerary in advance.'),
            Text('• Carry essential documents and cash.'),
            Text('• Respect local customs and traditions.'),
            Text('• Stay hydrated and protect yourself from the sun.'),
            Text('• Keep emergency contacts handy.'),
            // ...add more advice as needed...
          ],
        ),
      ),
    );
  }
}

