import 'package:flutter_test/flutter_test.dart';
import 'package:jaynes_max_tv/main.dart';

void main() {
  testWidgets('App launches', (WidgetTester tester) async {
    await tester.pumpWidget(const JaynesMaxTvApp());
    expect(find.byType(JaynesMaxTvApp), findsOneWidget);
  });
}
