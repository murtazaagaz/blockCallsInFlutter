import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_contacts/flutter_contacts.dart';

class Home extends StatefulWidget {
  Home({Key? key}) : super(key: key);

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  static const platform = MethodChannel('murtaza.com/block');

  TextEditingController controller = TextEditingController();
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        margin: const EdgeInsets.all(20),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: controller,
              decoration:
                  const InputDecoration(hintText: 'Enter Mobile Number'),
            ),
            
            ElevatedButton(
              onPressed: () async {
                accessPlatformCode('REQ');
              },
              child: const Center(
                child: Text('REQUEST'),
              ),
            ),
            const SizedBox(
              height: 30,
            ),
            ElevatedButton(
              onPressed: () async {
                accessPlatformCode('BLOCK');
              },
              child: const Center(
                child: Text('BLOCK'),
              ),
            ),
            const SizedBox(
              height: 30,
            ),
            ElevatedButton(
              onPressed: () async {
                accessPlatformCode('UNBLOCK');
              },
              child: const Center(
                child: Text('UNBLOCK'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void accessPlatformCode(String method) async {
    await platform
        .invokeMethod('$method', {'number': controller.text.toString()});
  }
}
